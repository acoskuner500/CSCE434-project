package ir.optimizations;

import java.util.List;
import java.util.HashSet;

import coco.Symbol;
import coco.SymbolTable;
import ir.cfg.BasicBlock;
import ir.cfg.CFG;
import ir.cfg.CFGVisitor;
import ir.cfg.Successor;
import ir.tac.*;
import types.ArrayType;

public class LiveVarsGenerator implements CFGVisitor {

    private boolean noChanges;
    private boolean noFunc;
    private List<Symbol> globalVars;
    private CFG currCFG;

    public void generate(CFG cfg) {
        // Run until no more changes
        this.noFunc = false;
        this.globalVars = null;
        this.currCFG = cfg;
        do {
            noChanges = true;
            cfg.start().accept(this);
            cfg.resetVisited();
        } while (!noChanges);
    }
    
    public void generate(CFG cfg, List<Symbol> globalSymbols) {
        // Run until no more changes
        this.noFunc = false;
        this.globalVars = globalSymbols;
        this.currCFG = cfg;
        do {
            noChanges = true;
            cfg.start().accept(this);
            cfg.resetVisited();
        } while (!noChanges);
    }

    public void generate(CFG cfg, List<Symbol> globalSymbols, boolean noFunc) {
        // Run until no more changes
        this.noFunc = noFunc;
        this.globalVars = globalSymbols;
        this.currCFG = cfg;
        do {
            noChanges = true;
            cfg.start().accept(this);
            cfg.resetVisited();
        } while (!noChanges);
    }

    @Override
    public void visit(BasicBlock block) {
        // Make sure to evaluate leafs first
        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }

        // Shallow copy of exit vars list
        HashSet<Symbol> exitSet = block.getExitLiveSet();
        HashSet<Symbol> tempSet;

        // If exit set uninitialized, clear it
        if (exitSet.contains(BasicBlock.unitializedSymbol)) {
            exitSet.clear();
            noChanges = false;
            tempSet = exitSet;
        } else {
            tempSet = new HashSet<Symbol>();
        }

        // Leaf node, exit set should have globals (non-main)
        if (globalVars != null && currCFG.function() != SymbolTable.mainSymbol && block.getSuccessors().isEmpty()) {
            // System.out.println("ADDING");
            tempSet.addAll(globalVars);
        } else {
            // Build exit set; get union of successors
            for (Successor s : block.getSuccessors()) {
                HashSet<Symbol> otherEntrySet = s.destination().getEntryLiveSet();

                // Only add successor data if initialized
                if (!otherEntrySet.contains(BasicBlock.unitializedSymbol)) {
                    // Add all data
                    tempSet.addAll(otherEntrySet);
                }
            }
        }

        // Check if new set is different from old set
        if (!tempSet.equals(exitSet)) {
            exitSet.clear();
            exitSet.addAll(tempSet);
            noChanges = false;
        }

        // Build entry set starting from exit set
        HashSet<Symbol> entrySet = block.getEntryLiveSet();
        entrySet.clear();
        entrySet.addAll(exitSet);

        // System.out.println("EXIT LIVE SET FOR BLOCK " + block.blockNumber() + ":");
        // for (Symbol s : entrySet) {
        //     System.out.print(s.name() + " ");
        // }  
        // System.out.println();

        // Iterate backwards through block instructions
        List<TAC> blockInstr = block.getInstructions();
        for (int i = blockInstr.size() - 1; i >= 0; i--) {
            TAC tac = blockInstr.get(i);

            // Don't look at already eliminated instructions
            if (tac.isEliminated()) {
                continue;
            }

            if (tac instanceof Assign) {
                Assign aTac = (Assign) tac;
                Variable dest = aTac.destination();

                // If reassigned, it is no longer live
                entrySet.remove(dest.symbol());

                // If operand, it is now live
                if (aTac.leftOperand() instanceof Variable) {
                    Variable lVar = (Variable) aTac.leftOperand();
                    entrySet.add(lVar.symbol());
                }
                if (!(aTac instanceof Move) && aTac.rightOperand() instanceof Variable) {
                    Variable rVar = (Variable) aTac.rightOperand();

                    // Don't count arrays since they live in memory
                    if (!(rVar.symbol().type() instanceof ArrayType)) {
                        entrySet.add(rVar.symbol());
                    }
                }
            } else if (tac instanceof Jump) {
                Jump jTac = (Jump) tac;

                // If used in comparison, is live
                if (jTac.comparison() instanceof Variable) {
                    Variable compVar = (Variable) jTac.comparison();
                    entrySet.add(compVar.symbol());
                }
            } else if (tac instanceof Call) {
                Call cTac = (Call) tac;
                List<Value> args = cTac.arguments().values();

                // If reassigned, it is no longer live
                if (cTac.destination() != null) {
                    entrySet.remove(cTac.destination().symbol());
                }

                for (Value arg : args) {
                    // If used as arg, is live
                    if (arg instanceof Variable) {
                        Variable argVar = (Variable) arg;
                        entrySet.add(argVar.symbol());
                    }
                }

                // Check if any variables in the function call are live
                if (!noFunc) {
                    HashSet<Symbol> funcLiveSet = cTac.functionCFG().start().getEntryLiveSet();
                    if (!funcLiveSet.contains(BasicBlock.unitializedSymbol)) {
                        entrySet.addAll(funcLiveSet);
                    }
                }
            } else if (tac instanceof Return) {
                Return rTac = (Return) tac;

                // If returned, is live
                if (rTac.returnValue() instanceof Variable) {
                    Variable retVar = (Variable) rTac.returnValue();
                    entrySet.add(retVar.symbol());
                }
            } else if (tac instanceof Store) {
                Store sTac = (Store) tac;

                // If stored, is live
                if (sTac.value() instanceof Variable) {
                    Variable storeVar = (Variable) sTac.value();
                    entrySet.add(storeVar.symbol());
                }

                // Location is also live
                entrySet.add(sTac.location().symbol());
            } else if (tac instanceof Print) {
                Print pTac = (Print) tac;

                // If printed, is live
                if (pTac.argument() instanceof Variable) {
                    Variable argVar = (Variable) pTac.argument();
                    entrySet.add(argVar.symbol());
                }
            } else if (tac instanceof Input) {
                Input iTac = (Input) tac;
                entrySet.remove(iTac.destination().symbol());
            } else if (tac instanceof Load) {
                Load lTac = (Load) tac;
                entrySet.remove(lTac.destination().symbol());
                entrySet.add(lTac.location().symbol());
            }
        }

        // System.out.println("ENTRY LIVE SET FOR BLOCK " + block.blockNumber() + ":");
        // for (Symbol s : entrySet) {
        //     System.out.print(s.name() + " ");
        // }  
        // System.out.println();
    }
}
