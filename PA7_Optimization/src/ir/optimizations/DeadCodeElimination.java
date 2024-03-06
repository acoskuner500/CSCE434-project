package ir.optimizations;

import java.util.List;
import java.util.HashSet;

import coco.Symbol;
import ir.cfg.BasicBlock;
import ir.cfg.CFG;
import ir.cfg.Successor;
import ir.cfg.CFGVisitor;
import ir.tac.*;
import types.ArrayType;

public class DeadCodeElimination implements CFGVisitor {

    private boolean noChanges;

    public boolean optimize (CFG cfg, List<Symbol> globalSymbols) {
        int attempts = 0;
        LiveVarsGenerator liveVarGen = new LiveVarsGenerator();

        do {
            attempts++;
            noChanges = true;
            liveVarGen.generate(cfg, globalSymbols);
            cfg.start().accept(this);
            cfg.resetLiveSet();
        } while (!noChanges);

        return attempts > 1;
    }

    @Override
    public void visit(BasicBlock block) {
        // Evaluate leaf nodes first
        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }

        // Get live vars starting at exit
        HashSet<Symbol> currLive = new HashSet<Symbol>();
        currLive.addAll(block.getExitLiveSet());
        List<TAC> blockInstr = block.getInstructions();

        // Iterate backwards through block instructions
        for (int i = blockInstr.size() - 1; i >= 0; i--) {
            TAC tac = blockInstr.get(i);

            // System.out.print("LIVE SET BEFORE INSTR " + i + ": ");
            // for (Symbol s : currLive) {
            //     System.out.print(s.name() + " ");
            // }
            // System.out.println();

            // Don't look at already eliminated instructions
            if (tac.isEliminated()) {
                continue;
            }

            // Remove useless assign
            if (tac instanceof Assign) {
                Assign aTac = (Assign) tac;

                // If assigning a variable to itself, can remove
                if (aTac instanceof Move && aTac.destination().equals(aTac.leftOperand())) {
                    aTac.eliminate();
                    noChanges = false;
                } else if (!currLive.contains(aTac.destination().symbol())) {
                    tac.eliminate();
                    noChanges = false;
                } else {
                    currLive.remove(aTac.destination().symbol());
                    // If not eliminated, then any used variables are live
                    if (aTac.leftOperand() instanceof Variable) {
                        Variable lVar = (Variable) aTac.leftOperand();
                        currLive.add(lVar.symbol());
                    }
                    if (!(aTac instanceof Move) && aTac.rightOperand() instanceof Variable) {
                        Variable rVar = (Variable) aTac.rightOperand();

                        // Don't count arrays since they live in memory
                        if (!(rVar.symbol().type() instanceof ArrayType)) {
                            currLive.add(rVar.symbol());
                        }
                    }
                }
            } else if (tac instanceof Jump) {
                Jump jTac = (Jump) tac;

                // If used in comparison, is live
                if (jTac.comparison() instanceof Variable) {
                    Variable compVar = (Variable) jTac.comparison();
                    currLive.add(compVar.symbol());
                }
            } else if (tac instanceof Call) {
                Call cTac = (Call) tac;
                List<Value> args = cTac.arguments().values();

                // If reassigned, it is no longer live
                if (cTac.destination() != null) {
                    currLive.remove(cTac.destination().symbol());
                }

                for (Value arg : args) {
                    // If used as arg, is live
                    if (arg instanceof Variable) {
                        Variable argVar = (Variable) arg;
                        currLive.add(argVar.symbol());
                    }
                }

                // Check if any variables in the function call are live
                HashSet<Symbol> funcLiveSet = cTac.functionCFG().start().getEntryLiveSet();
                currLive.addAll(funcLiveSet);
            } else if (tac instanceof Return) {
                Return rTac = (Return) tac;

                // If returned, is live
                if (rTac.returnValue() instanceof Variable) {
                    Variable retVar = (Variable) rTac.returnValue();
                    currLive.add(retVar.symbol());
                }
            } else if (tac instanceof Store) {
                Store sTac = (Store) tac;

                // If stored, is live
                if (sTac.value() instanceof Variable) {
                    Variable storeVar = (Variable) sTac.value();
                    currLive.add(storeVar.symbol());
                }

                // Location is also live
                currLive.add(sTac.location().symbol());
            } else if (tac instanceof Print) {
                Print pTac = (Print) tac;

                // If printed, is live
                if (pTac.argument() instanceof Variable) {
                    Variable argVar = (Variable) pTac.argument();
                    currLive.add(argVar.symbol());
                }
            } else if (tac instanceof Input) {
                Input iTac = (Input) tac;

                // If read into, no longer live
                currLive.remove(iTac.destination().symbol());
            } else if (tac instanceof Load) {
                Load lTac = (Load) tac;
                currLive.remove(lTac.destination().symbol());
                currLive.add(lTac.location().symbol());
            }
        }
    } 
}
