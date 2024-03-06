package ir.optimizations;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import ir.cfg.CFGVisitor;
import ir.cfg.Successor;
import ir.cfg.BasicBlock;
import ir.cfg.CFG;
import ir.tac.*;
import coco.Symbol;

public class ValueSetGenerator implements CFGVisitor {
    
    private boolean noChanges;

    public void generate(CFG cfg) {
        do {
            noChanges = true;
            cfg.start().accept(this);
            cfg.resetVisited();
        } while (!noChanges);
    }

    @Override
    public void visit(BasicBlock block) {
        HashMap<Symbol, Value> entrySet = block.getEntryValueSet();
        boolean first;

        // System.out.println("ENTRY SET FOR BLOCK " + block.blockNumber() + ":");
        // for (Map.Entry<Symbol, Value> e : entrySet.entrySet()) {
        //     System.out.println("\t" + e.getKey() + ": " + e.getValue());
        // }
        // System.out.println();

        // Build entry set
        // If uninitialized, clear it
        if (entrySet.containsKey(BasicBlock.unitializedSymbol)) {
            entrySet.clear();
            first = true;
            noChanges = false;

            // Get intersections of predecessors (if defined)
            for (BasicBlock pBlock : block.getPredecessors()) {
                HashMap<Symbol, Value> otherExitSet = pBlock.getExitValueSet();

                // Only add predecessor data if initialized
                if (!otherExitSet.containsKey(BasicBlock.unitializedSymbol)) {
                    // Add all the data from the first predecessor
                    if (first) {
                        entrySet.putAll(otherExitSet);
                        first = false;
                    } else {
                        // Remove value if not also in other set
                        ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                        for (Map.Entry<Symbol, Value> e : entrySet.entrySet()) {
                            if (!otherExitSet.containsKey(e.getKey())) {
                                toRemove.add(e.getKey());
                            }
                        }

                        for (Symbol sym : toRemove) {
                            entrySet.remove(sym);
                        }
                    }
                }
            }
        } else {
            HashMap<Symbol, Value> tempSet = new HashMap<Symbol, Value>();
            first = true;

            // Get intersection of predecessors
            for (BasicBlock pBlock : block.getPredecessors()) {
                HashMap<Symbol, Value> otherExitSet = pBlock.getExitValueSet();

                if (first) {
                    tempSet.putAll(otherExitSet);
                    first = false;
                } else {
                    // Remove value if not also in other set
                    ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                    for (Map.Entry<Symbol, Value> e : tempSet.entrySet()) {
                        if (!otherExitSet.containsKey(e.getKey())) {
                            toRemove.add(e.getKey());
                        } else if (otherExitSet.get(e.getKey()) instanceof Literal && e.getValue() instanceof Literal) {
                            Literal firstLit = (Literal) otherExitSet.get(e.getKey());
                            Literal secondLit = (Literal) e.getValue();

                            if (firstLit.value() != secondLit.value()) {
                                toRemove.add(e.getKey());
                            }
                        } else if (otherExitSet.get(e.getKey()) instanceof Variable && e.getValue() instanceof Variable) {
                            Variable firstVar = (Variable) otherExitSet.get(e.getKey());
                            Variable secondVar = (Variable) e.getValue();

                            if (firstVar.symbol() != secondVar.symbol()) {
                                toRemove.add(e.getKey());
                            }
                        } else {
                            toRemove.add(e.getKey());
                        }
                    }

                    for (Symbol sym: toRemove) {
                        tempSet.remove(sym);
                    }
                }
            }

            // Check if new set is different from old set
            if (!tempSet.equals(entrySet)) {
                entrySet.clear();
                entrySet.putAll(tempSet);
                noChanges = false;
            }
        }

        // Build exit set starting from entry set
        HashMap<Symbol, Value> exitSet = block.getExitValueSet();
        exitSet.clear();
        exitSet.putAll(entrySet);

        for (TAC tac : block) {
            // Log new constant/copy or remove from set if changed
            if (tac instanceof Assign) {
                Assign aTac = (Assign) tac;
                
                if (tac instanceof Move) {
                    // Log new constant/copy
                    exitSet.put(aTac.destination().symbol(), aTac.leftOperand());
                } else {
                    exitSet.remove(aTac.destination().symbol());
                }

                // Need to remove any copies of this variable
                ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                for (Map.Entry<Symbol, Value> e : exitSet.entrySet()) {
                    if (e.getValue() instanceof Variable) {
                        Variable eVar = (Variable) e.getValue();

                        if (aTac.destination().symbol() == eVar.symbol()) {
                            toRemove.add(e.getKey());
                        }
                    }
                }

                for (Symbol s : toRemove) {
                    exitSet.remove(s);
                }
            } 
            // Remove any constants changed by function call
            else if (tac instanceof Call) {
                Call cTac = (Call) tac;

                for (Symbol s : cTac.functionCFG().reassignedVariables()) {
                    exitSet.remove(s);
                }

                if (cTac.destination() != null) {
                    exitSet.remove(cTac.destination().symbol());

                    // Need to remove any copies of this variable
                    ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                    for (Map.Entry<Symbol, Value> e : exitSet.entrySet()) {
                        if (e.getValue() instanceof Variable) {
                            Variable eVar = (Variable) e.getValue();

                            if (cTac.destination().symbol() == eVar.symbol()) {
                                toRemove.add(e.getKey());
                            }
                        }
                    }

                    for (Symbol s : toRemove) {
                        exitSet.remove(s);
                    }
                }
            }
            // Remove any replaced by load
            else if (tac instanceof Load) {
                Load lTac = (Load) tac;

                exitSet.remove(lTac.destination().symbol());

                // Need to remove any copies of this variable
                ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                for (Map.Entry<Symbol, Value> e : exitSet.entrySet()) {
                    if (e.getValue() instanceof Variable) {
                        Variable eVar = (Variable) e.getValue();

                        if (lTac.destination().symbol() == eVar.symbol()) {
                            toRemove.add(e.getKey());
                        }
                    }
                }

                for (Symbol s : toRemove) {
                    exitSet.remove(s);
                }
            }
        }

        // System.out.println("EXIT SET FOR BLOCK " + block.blockNumber() + ":");
        // for (Map.Entry<Symbol, Value> e : exitSet.entrySet()) {
        //     System.out.println("\t" + e.getKey() + ": " + e.getValue());
        // }
        // System.out.println();

        // Generate for successor blocks
        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }
    }
}