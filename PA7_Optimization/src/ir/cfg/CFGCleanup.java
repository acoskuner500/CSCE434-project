package ir.cfg;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import ir.tac.Jump;
import ir.tac.Call;
import ir.tac.TAC;
import ir.tac.Bra;
import coco.Symbol;
import coco.SymbolTable;
import ir.SSA;

public class CFGCleanup implements CFGVisitor {

    private boolean noChanges;
    private HashSet<Symbol> usedFunc;
    private boolean getUsedFuncs;
    private CFG currCFG;

    public void clean(SSA ssa) {
        usedFunc = new HashSet<Symbol>();

        // Minimize the number of basic blocks
        getUsedFuncs = false;
        for (CFG cfg : ssa) {
            currCFG = cfg;

            do {
                noChanges = true;
                cfg.start().accept(this);
                cfg.resetVisited();
            } while (!noChanges);
        }
        ssa.resetVisited();

        // Eliminate orphan functions
        getUsedFuncs = true;
        Queue<CFG> toVisit = new LinkedList<CFG>();
        HashSet<CFG> visited = new HashSet<CFG>();

        do {
            noChanges = true;

            // Start by checking which functions are called by main
            toVisit.add(ssa.mainCFG());

            // Keep checking for anymore CFGs to visit
            while(!toVisit.isEmpty()) {
                CFG curr = toVisit.remove();
                visited.add(curr);

                // Determine which functions get used
                curr.start().accept(this);

                // Find which CFGs need to be visited next
                for (CFG cfg : ssa) {
                    if(usedFunc.contains(cfg.function()) && !visited.contains(cfg)) {
                        toVisit.add(cfg);
                    }
                }
            }

            // Remove orphan functions
            List<CFG> toRemove = new ArrayList<CFG>();
            for (CFG cfg : ssa) {
                if (cfg.function() != SymbolTable.mainSymbol && !usedFunc.contains(cfg.function())) {
                    toRemove.add(cfg);
                }
            }

            List<CFG> funcs = ssa.CFGs();
            for (CFG cfg : toRemove) {
                funcs.remove(cfg);
                noChanges = false;
            }

            for (CFG cfg : ssa) {
                cfg.resetVisited();
            }

            visited.clear();
            usedFunc.clear();
        } while (!noChanges);
    }

    @Override
    public void visit(BasicBlock block) {
        // Check for function calls
        if (getUsedFuncs) {
            for (TAC tac : block) {
                if (tac instanceof Call) {
                    Call cTac = (Call) tac;
                    usedFunc.add(cTac.function());
                }
            }
        }
        // Check if block is empty
        else if (block.getInstructions().isEmpty()) {
            // If empty can have at most 1 successor
            List<Successor> sBlocks = block.getSuccessors();
            if (!sBlocks.isEmpty()) {
                // If there is a successor, change predecessors to point to it
                Successor successor = sBlocks.get(0);

                for (BasicBlock pBlock : block.getPredecessors()) {
                    List<Successor> pSuccessors = pBlock.getSuccessors();

                    // Remove connections to this block
                    int toRemove;
                    for (toRemove = 0; toRemove < pSuccessors.size(); toRemove++) {
                        if (pSuccessors.get(toRemove).destination() == block) {
                            break;
                        }
                    }

                    Successor oldSuccessor = pSuccessors.get(toRemove);
                    pSuccessors.remove(toRemove);

                    // Point to successor
                    pBlock.addSuccessor(successor.destination(), oldSuccessor.label(), oldSuccessor.arrowType());
                    successor.destination().addPredecessor(pBlock);

                    List<TAC> pInstr = pBlock.getInstructions();
                    if (!pInstr.isEmpty()) {
                        // Update jump instructions if necessary
                        TAC lastInstr = pInstr.get(pInstr.size() - 1);
                        if (lastInstr instanceof Jump) {
                            Jump jTac = (Jump) lastInstr;
                            jTac.setJumpDestination(successor.destination());
                        } else if (lastInstr instanceof Bra) {
                            Bra bTac = (Bra) lastInstr;
                            bTac.setJump(successor.destination());
                        }
                    }
                }

                noChanges = false;
            }
        } else {
            // See if blocks are not minimal and need to be merged
            List<Successor> successors = block.getSuccessors();
            BasicBlock successor;

            if (successors.size() == 1 && (successor = successors.get(0).destination()).getPredecessors().size() == 1 && successor != currCFG.start()) {
                // Merge the blocks
                List<TAC> blockInstr = block.getInstructions();

                // If last instruction is branch, eliminate
                if (blockInstr.get(blockInstr.size() - 1) instanceof Bra) {
                    blockInstr.get(blockInstr.size() - 1).eliminate();
                }

                for (TAC tac : successor) {
                    blockInstr.add(tac);
                }

                // Remove old connections
                successors.clear();
                successor.getPredecessors().clear();
                
                for (Successor newSuccessor : successor.getSuccessors()) {
                    successors.add(newSuccessor);
                    newSuccessor.destination().getPredecessors().remove(successor);
                    newSuccessor.destination().getPredecessors().add(block);
                }

                noChanges = false;
            }
        }

        List<BasicBlock> nextBlocks = new ArrayList<BasicBlock>();

        for (Successor s : block.getSuccessors()) {
            // Get successor blocks for later (may be removed)
            nextBlocks.add(s.destination());
        }

        for (BasicBlock bb : nextBlocks) {
            bb.accept(this);
        }
    }
}
