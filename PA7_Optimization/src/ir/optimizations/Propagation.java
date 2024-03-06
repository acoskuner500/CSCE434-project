package ir.optimizations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import ir.cfg.BasicBlock;
import ir.cfg.CFGVisitor;
import ir.cfg.Successor;
import ir.cfg.CFG;
import ir.tac.*;
import coco.Symbol;

public class Propagation implements CFGVisitor {

    public enum PropagationMode {
        CP,
        CPP
    }

    private boolean noChanges;
    private PropagationMode mode;

    private boolean optimize(CFG cfg, PropagationMode mode) {
        int attempts = 0;
        ValueSetGenerator constSetGen = new ValueSetGenerator();
        ReassignedSetGenerator reassignSetGen = new ReassignedSetGenerator();
        this.mode = mode;

        do {
            attempts++;
            noChanges = true;
            reassignSetGen.generate(cfg);
            constSetGen.generate(cfg);
            cfg.start().accept(this);
            cfg.resetValueSet();
        } while (!noChanges);

        return attempts > 1;
    }

    public boolean optimizeCP(CFG cfg) {
        return optimize(cfg, PropagationMode.CP);
    }

    public boolean optimizeCPP(CFG cfg) {
        return optimize(cfg, PropagationMode.CPP);
    }
   
    @Override
    public void visit(BasicBlock block) {
        HashMap<Symbol, Value> currSet = new HashMap<Symbol, Value>();
        currSet.putAll(block.getEntryValueSet());

        // System.out.println("ENTRY SET FOR BLOCK " + block.blockNumber() + ":");
        // for (Map.Entry<Symbol, Value> e : currSet.entrySet()) {
        //     System.out.println("\t" + e.getKey() + ": " + e.getValue());
        // }
        // System.out.println();

        // After getting the constant set for the block, determine if you can replace any operands
        for (TAC tac : block) {
            if (tac instanceof Assign) {
                Assign aTac = (Assign) tac;

                // Check if left operand can be replaced
                if (aTac.leftOperand() instanceof Variable) {
                    Symbol left = ((Variable) aTac.leftOperand()).symbol();
                    if (currSet.containsKey(left)) {
                        Value sub = currSet.get(left);

                        if ((sub instanceof Literal && mode == PropagationMode.CP) || (sub instanceof Variable && mode == PropagationMode.CPP)) {    
                            aTac.setLeftOperand(sub);
                            noChanges = false;
                        }
                    }
                }

                // Check if right operand can be replaced (Only if not Move)
                if (!(tac instanceof Move) && aTac.rightOperand() instanceof Variable) {
                    Symbol right = ((Variable) aTac.rightOperand()).symbol();
                    if (currSet.containsKey(right)) {
                        Value sub = currSet.get(right);

                        if ((sub instanceof Literal && mode == PropagationMode.CP) || (sub instanceof Variable && mode == PropagationMode.CPP)) {    
                            aTac.setRightOperand(sub);
                            noChanges = false;
                        }
                    }
                }

                // Log new constant or remove from set if changed
                if (tac instanceof Move) {
                    if (!(aTac.leftOperand() instanceof Temporary)) {
                        currSet.put(aTac.destination().symbol(), aTac.leftOperand());
                    }
                } else {
                    currSet.remove(aTac.destination().symbol());
                }

                // If changed, unregister any copies
                ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                for (Map.Entry<Symbol, Value> e : currSet.entrySet()) {
                    if (e.getValue() instanceof Variable) {
                        Variable eVar = (Variable) e.getValue();

                        if (aTac.destination().symbol() == eVar.symbol()) {
                            toRemove.add(e.getKey());
                        }
                    }
                }

                for (Symbol s : toRemove) {
                    currSet.remove(s);
                }
            } else if (tac instanceof Jump) {
                Jump jTac = (Jump) tac;
                
                // Check if comparison can be replaced
                if (jTac.comparison() instanceof Variable) {
                    Symbol comp = ((Variable) jTac.comparison()).symbol();
                    if (currSet.containsKey(comp)) {
                        Value sub = currSet.get(comp);

                        if ((sub instanceof Literal && mode == PropagationMode.CP) || (sub instanceof Variable && mode == PropagationMode.CPP)) {    
                            jTac.setComparison(sub);
                            noChanges = false;
                        }
                    }
                }
            } else if (tac instanceof Call) {
                Call cTac = (Call) tac;

                // Check if any parameters can be replaced
                List<Value> args = cTac.arguments().values();
                for (int i = 0; i < args.size(); i++) {
                    if (args.get(i) instanceof Variable) {
                        Symbol arg = ((Variable) args.get(i)).symbol();
                        if (currSet.containsKey(arg)) {
                            Value sub = currSet.get(arg);

                            if ((sub instanceof Literal && mode == PropagationMode.CP) || (sub instanceof Variable && mode == PropagationMode.CPP)) {    
                                args.set(i, sub);
                                noChanges = false;
                            }
                        }
                    }
                }

                // Remove any variables the function changes
                for (Symbol s : cTac.functionCFG().reassignedVariables()) {
                    currSet.remove(s);
                }

                if (cTac.destination() != null) {
                    currSet.remove(cTac.destination().symbol());

                    // Need to remove any copies of this variable
                    ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                    for (Map.Entry<Symbol, Value> e : currSet.entrySet()) {
                        if (e.getValue() instanceof Variable) {
                            Variable eVar = (Variable) e.getValue();

                            if (cTac.destination().symbol() == eVar.symbol()) {
                                toRemove.add(e.getKey());
                            }
                        }
                    }

                    for (Symbol s : toRemove) {
                        currSet.remove(s);
                    }
                }
            } else if (tac instanceof Return) {
                Return rTac = (Return) tac;

                // Check if return can be replaced
                if (rTac.returnValue() instanceof Variable) {
                    Symbol val = ((Variable) rTac.returnValue()).symbol();
                    if (currSet.containsKey(val)) {
                        Value sub = currSet.get(val);

                        if ((sub instanceof Literal && mode == PropagationMode.CP) || (sub instanceof Variable && mode == PropagationMode.CPP)) {    
                            rTac.setReturn(sub);
                            noChanges = false;
                        }
                    }
                }
            } else if (tac instanceof Store) {
                Store sTac = (Store) tac;
                
                // Check if value can be replaced
                if (sTac.value() instanceof Variable) {
                    Symbol val = ((Variable) sTac.value()).symbol();
                    if (currSet.containsKey(val)) {
                        Value sub = currSet.get(val);

                        if ((sub instanceof Literal && mode == PropagationMode.CP) || (sub instanceof Variable && mode == PropagationMode.CPP)) {    
                            sTac.setValue(sub);
                            noChanges = false;
                        }
                    }
                }
            } else if (tac instanceof Print) {
                Print pTac = (Print) tac;

                // Check if arg can be replaced
                if (pTac.argument() instanceof Variable) {
                    Symbol arg = ((Variable) pTac.argument()).symbol();
                    if (currSet.containsKey(arg)) {
                        Value sub = currSet.get(arg);

                        if ((sub instanceof Literal && mode == PropagationMode.CP) || (sub instanceof Variable && mode == PropagationMode.CPP)) {    
                            pTac.setArgument(sub);
                            noChanges = false;
                        }
                    }
                }
            } else if (tac instanceof Input) {
                Input iTac = (Input) tac;

                // Invalidate old value
                currSet.remove(iTac.destination().symbol());
            } else if (tac instanceof Load) {
                Load lTac = (Load) tac;

                currSet.remove(lTac.destination().symbol());

                // Need to remove any copies of this variable
                ArrayList<Symbol> toRemove = new ArrayList<Symbol>();

                for (Map.Entry<Symbol, Value> e : currSet.entrySet()) {
                    if (e.getValue() instanceof Variable) {
                        Variable eVar = (Variable) e.getValue();

                        if (lTac.destination().symbol() == eVar.symbol()) {
                            toRemove.add(e.getKey());
                        }
                    }
                }

                for (Symbol s : toRemove) {
                    currSet.remove(s);
                }
            }
        }

        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }
    }
}
