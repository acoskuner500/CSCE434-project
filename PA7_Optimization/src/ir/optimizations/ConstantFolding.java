package ir.optimizations;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import ir.cfg.CFG;
import ir.cfg.BasicBlock;
import ir.cfg.CFGVisitor;
import ir.cfg.Successor;
import ast.IntegerLiteral;
import ir.tac.*;
import ir.tac.Jump.JumpType;

public class ConstantFolding implements TACVisitor, CFGVisitor {

    private boolean noChanges;
    private BasicBlock currBlock;
    private int currIdx;
    private List<BasicBlock> makeUnconditionalJump = new ArrayList<BasicBlock>();
   
    public boolean optimize(CFG cfg) {
        int attempts = 0;
        do {
            attempts++;
            noChanges = true;
            cfg.start().accept(this);

            // Fix repeat jumps
            for (BasicBlock bb : makeUnconditionalJump) {
                List<TAC> bbInstr = bb.getInstructions();

                if (bbInstr.get(bbInstr.size() - 1) instanceof Jump) {
                    Jump instr = (Jump) bbInstr.get(bbInstr.size() - 1);
                    bbInstr.add(new Bra(instr.getID(), instr.jumpDestination()));
                } else {
                    // TODO: Remove if this is proven to not be possible
                    System.err.println("LAST INSTRUCTION OF BLOCK " + bb.blockNumber() + " IS NOT JUMP");
                }
            }

            makeUnconditionalJump.clear();
        } while (!noChanges);

        return attempts > 1;
    }

    private void foldAssign(int id, Variable destination, Value source) {
        Move mTac = new Move(id, destination, source);
        currBlock.getInstructions().set(currIdx, mTac);
        noChanges = false;
    }

    private void foldRelation(Jump instr, boolean t) {
        BasicBlock jumpBlock = instr.jumpDestination();
        List<Successor> successors = currBlock.getSuccessors();
        int jumpIdx = (successors.get(0).destination() == jumpBlock) ? 0 : 1;
        int nextIdx = (jumpIdx + 1) % 2;
        BasicBlock nextBlock = successors.get(nextIdx).destination();

        // Find intersection between paths (if it exists)
        BasicBlock intersect = null;
        HashSet<BasicBlock> inPath = new HashSet<BasicBlock>();

        // Get all reachable from one path
        findReachable(nextBlock, inPath);
        // Find intersection
        if (instr.jumpType() == JumpType.IF_ELSE) {
            intersect = findFirstIntersect(instr.jumpDestination(), inPath);
        }

        int i;
        List<BasicBlock> nextPreds = nextBlock.getPredecessors();
        List<BasicBlock> jumpPreds = jumpBlock.getPredecessors();
        List<BasicBlock> intersectPreds = intersect != null ? intersect.getPredecessors() : null;
        List<BasicBlock> currPreds = currBlock.getPredecessors();

        // If condition is true, then only need jump path
        if (t) {
            switch (instr.jumpType()) {
                case IF_THEN:
                    // Remove THEN path as successor of jump instruction
                    successors.remove(0);

                    // TODO: Remove this if it is proven to never happen
                    if (jumpPreds.size() != 2) {
                        System.err.println("ELSE BLOCK FOR IF_THEN HAS " + jumpPreds.size() + " PREDECESSOR(S)");
                        return;
                    }

                    // Remove THEN path as predecessor of jump block
                    jumpPreds.remove(1);

                    break;
                case IF_ELSE:
                    // Remove THEN path as successor of jump instruction
                    successors.remove(0);

                    // TODO: Remove this if it is proven to never happen
                    if (intersectPreds.size() != 2) {
                        System.err.println("INTERSECT BLOCK FOR IF_ELSE HAS " + intersectPreds.size() + " PREDECESSOR(S)");
                        return;
                    }

                    // Remove THEN path as predecessor of intersection between paths
                    intersectPreds.remove(0);

                    break;
                case WHILE:
                    // Remove THEN path as successor of jump instruction
                    successors.remove(0);

                    // TODO: Remove this if it is proved to never happen
                    if (currPreds.size() != 2) {
                        System.err.println("COMP BLOCK FOR WHILE HAS " + currPreds.size() + " PREDECESSOR(S)");
                        return; 
                    }

                    // Remove THEN path as predecessor of jump instruction
                    currPreds.remove(1);

                    break;
                case REPEAT:
                    // Remove ELSE path as successor of jump instruction
                    successors.remove(1);

                    // Change label for jump
                    successors.get(0).setLabel("");

                    // TODO: Remove this if it is proved to never happen
                    if (nextPreds.size() != 1) {
                        System.err.println("ELSE BLOCK FOR REPEAT HAS " + nextPreds.size() + " PREDECESSOR(S)");
                    }

                    // Remove jump instruction as predecessor of ELSE
                    nextPreds.remove(0);

                    // Replace with unconditional jump later
                    makeUnconditionalJump.add(currBlock);
            }
        }
        // Otherwise, will never jump
        else {
            switch (instr.jumpType()) {
                case IF_THEN:
                    // Remove jump path as successor of jump instruction
                    successors.remove(1);

                    // TODO: Remove this if it is proven to never happen
                    if (jumpPreds.size() != 2) {
                        System.err.println("ELSE BLOCK FOR IF_THEN HAS " + jumpPreds.size() + " PREDECESSOR(S)");
                        return;
                    }

                    // Remove jump instruction as predecessor of jump block
                    jumpPreds.remove(0);
                    
                    break;
                case IF_ELSE:
                    // Remove ELSE path as successor of jump instruction
                    successors.remove(1);

                    // TODO: Remove this if it is proven to never happen
                    if (intersectPreds.size() != 2) {
                        System.err.println("INTERSECT BLOCK FOR IF_ELSE HAS " + intersectPreds.size() + " PREDECESSOR(S)");
                        return;
                    }

                    // Remove ELSE path as predecessor of intersection between paths
                    intersectPreds.remove(1);
                    break;
                case WHILE:
                    // Remove ELSE path as successor of jump instruction
                    successors.remove(1);

                    // TODO: Remove this if it is proved to never happen
                    if (currPreds.size() != 2) {
                        System.err.println("COMP BLOCK FOR WHILE HAS " + currPreds.size() + " PREDECESSOR(S)");
                        return; 
                    }

                    // Remove jump instruction as predecessor of ELSE path
                    jumpPreds.remove(0);

                    break;
                case REPEAT:
                    // Remove THEN path as successor of jump instruction
                    successors.remove(0);

                    // TODO: Remove this if it is proved to never happen
                    if (currPreds.size() == 0) {
                        System.err.println("CURR BLOCK FOR REPEAT HAS " + currPreds.size() + " PREDECESSOR(S)");
                    }

                    // Remove jump instruction as predecessor of loop
                    currPreds.remove(0);
            }
        }
    }

    private void foldRelationOld(Jump instr, boolean t) {
        BasicBlock jumpBlock = instr.jumpDestination();
        List<Successor> successors = currBlock.getSuccessors();
        int jumpIdx = (successors.get(0).destination() == jumpBlock) ? 0 : 1;
        List<BasicBlock> jumpPreds = jumpBlock.getPredecessors();
        BasicBlock thenBlock = successors.get((jumpIdx + 1) % 2).destination();

        // If condition is true, remove else
        if (t) {
            // Remove as successor for current block
            successors.remove(jumpIdx);

            // For IF-ELSE and WHILE
            if (jumpPreds.size() == 1) {
                // Need to find path intersection between if and else branches
                HashSet<BasicBlock> inPath = new HashSet<BasicBlock>();

                // Get all reachable from then
                findReachable(thenBlock, inPath);

                // Find intersection
                BasicBlock afterBlock = findFirstIntersect(instr.jumpDestination(), inPath);

                // If there is an intersection, then it is an IF-ELSE
                if (afterBlock == null) {
                    // Figure out which predecessor to remove from the afterBlock
                    int elseIdx;
                    List<TAC> tempInstr = afterBlock.getPredecessors().get(0).getInstructions();

                    // Else block has no jump to afterBlock
                    if (tempInstr.size() != 0 && (tempInstr.get(tempInstr.size() - 1) instanceof Bra)) {
                        elseIdx = 1;
                    } else {
                        elseIdx = 0;
                    }

                    // Remove else as prececessor of after
                    afterBlock.getPredecessors().remove(elseIdx);
                }
                // If there is no intersection, then it is a WHILE
            }

            // Remove predecessor
            jumpPreds.remove(currBlock);
        } else {
            // Remove predecessor
            thenBlock.getPredecessors().remove(currBlock);

            // For IF-THEN
            if (jumpPreds.size() > 1) {
                jumpPreds.remove(jumpPreds.get(0) == currBlock ? 1 : 0);
            } 
            // For WHILE 
            else if (jumpPreds.size() == 1) {
                // Remove loop edge
                List<BasicBlock> loopPred = currBlock.getPredecessors();
                if (loopPred.size() >= 1) {
                    loopPred.get(loopPred.size() - 1).getSuccessors().clear();
                    loopPred.remove(loopPred.size() - 1);
                }
            }

            // Remove as successor for current block
            successors.remove((jumpIdx + 1) % 2);
        } 
    }

    private void findReachable(BasicBlock curr, HashSet<BasicBlock> reached) {
        if (!reached.contains(curr)) {
            reached.add(curr);

            for (Successor s : curr.getSuccessors()) {
                findReachable(s.destination(), reached);
            }
        }
    }

    private BasicBlock findFirstIntersect(BasicBlock curr, HashSet<BasicBlock> reached) {
        if (reached.contains(curr)) {
            return curr;
        }
        
        for (Successor s : curr.getSuccessors()) {
            BasicBlock ret;
            if ((ret = findFirstIntersect(s.destination(), reached)) != null) {
                return ret;
            }
        }

        return null;
    }

    @Override
    public void visit(BasicBlock block) {
        currBlock = block;

        // Go through each instruction in the basic block
        currIdx = 0;
        for (TAC tac : block) {
            if (!tac.isEliminated()) {
                tac.accept(this);
            }
            currIdx++;
        }

        // Visit connected blocks
        for (Successor s : block.getSuccessors()) {
            s.destination().accept(this);
        }
    }

    @Override
    public void visit(Add instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();

            Literal result = new Literal(new IntegerLiteral(lVal + rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        } else if (instr.leftOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();

            // x = 0 + y => x = y
            if (lVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.rightOperand());
            }
        } else if (instr.rightOperand() instanceof Literal) {
            int rVal = ((Literal) instr.rightOperand()).value();

            // x = y + 0 => x = y
            if (rVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
        }
    }

    @Override
    public void visit(Adda instr) {}

    @Override
    public void visit(And instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();

            Literal result = new Literal(new IntegerLiteral(lVal & rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        } else if (instr.leftOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();

            // x = 0 and y => x = 0
            if (lVal == 0) {
                Literal result = new Literal(new IntegerLiteral(0));
                foldAssign(instr.getID(), instr.destination(), result);
            } 
            // x = 1 and y => x = y 
            else if (lVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.rightOperand());
            }
        } else if (instr.rightOperand() instanceof Literal) {
            int rVal = ((Literal) instr.rightOperand()).value();

            // x = y and 0 => x = 0
            if (rVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.rightOperand());
            }
            // x = y and 1 => x = y
            else if (rVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
        }
    }

    @Override
    public void visit(Cmp instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            int res = lVal - rVal;

            if (res > 1) {
                res = 1;
            } else if (res < -1) {
                res = -1;
            }

            Literal result = new Literal(new IntegerLiteral(res));
            foldAssign(instr.getID(), instr.destination(), result);
        }
    }

    @Override
    public void visit(Div instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result = new Literal(new IntegerLiteral(lVal / rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        } else if (instr.leftOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();

            // x = 0 / y => x = 0
            if (lVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
        } else if (instr.rightOperand() instanceof Literal) {
            int rVal = ((Literal) instr.rightOperand()).value();

            // x = y / 1 => x = y
            if (rVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
        } else {
            // x = y / y => x = 1
            if (((Variable) instr.leftOperand()).symbol() == ((Variable) instr.rightOperand()).symbol()) {
                Literal result = new Literal(new IntegerLiteral(1));
                foldAssign(instr.getID(), instr.destination(), result);
            }
        }
    }

    @Override
    public void visit(Lsh instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result;

            if (rVal >= 0) {
                result = new Literal(new IntegerLiteral(lVal >>> rVal));
            } else {
                result = new Literal(new IntegerLiteral(lVal << rVal));
            }

            foldAssign(instr.getID(), instr.destination(), result);
        }
    }

    @Override
    public void visit(Mod instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result = new Literal(new IntegerLiteral(lVal % rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        }
    }

    @Override
    public void visit(Move instr) {
        // Move can't be const folded
    }

    @Override
    public void visit(Mul instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result = new Literal(new IntegerLiteral(lVal * rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        } else if (instr.leftOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();

            // x = 1 * y => x = y
            if (lVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.rightOperand());
            }
            // x = 0 * y => 0
            else if (lVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
        } else if (instr.rightOperand() instanceof Literal) {
            int rVal = ((Literal) instr.rightOperand()).value();

            // x = y * 1 => x = y
            if (rVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
            // x = y * 0 => 0
            else if (rVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.rightOperand());
            }
        }
    }

    @Override
    public void visit(Or instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result = new Literal(new IntegerLiteral(lVal | rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        } else if (instr.leftOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();

            // x = 0 or y => y
            if (lVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.rightOperand());
            } 
            // x = 1 or y => x = 1
            else if (lVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
        } else if (instr.rightOperand() instanceof Literal) {
            int rVal = ((Literal) instr.rightOperand()).value();

            // x = y or 0 => y
            if (rVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
            // x = y or 1 => x = 1
            else if (rVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.rightOperand());
            }
        }
    }

    @Override
    public void visit(Pow instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result = new Literal(new IntegerLiteral((int) Math.pow(lVal, rVal)));
            foldAssign(instr.getID(), instr.destination(), result);
        } else if (instr.leftOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();

            // x = 0 ^ y => 0 | x = 1 ^ y => 1
            if (lVal == 0 || lVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            } 
        } else if (instr.rightOperand() instanceof Literal) {
            int rVal = ((Literal) instr.rightOperand()).value();

            // x = y ^ 1 => y
            if (rVal == 1) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
            // x = y ^ 0 => x = 1
            else if (rVal == 1) {
                Literal result = new Literal(new IntegerLiteral(1));
                foldAssign(instr.getID(), instr.destination(), result);
            }
        }
    }

    @Override
    public void visit(Sub instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result = new Literal(new IntegerLiteral(lVal - rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        } else if (instr.rightOperand() instanceof Literal) {
            int rVal = ((Literal) instr.rightOperand()).value();

            // x = y - 0 => y
            if (rVal == 0) {
                foldAssign(instr.getID(), instr.destination(), instr.leftOperand());
            }
        } else if (instr.leftOperand() instanceof Variable){
            // x = y - y => x = 0
            if (((Variable) instr.leftOperand()).symbol() == ((Variable) instr.rightOperand()).symbol()) {
                Literal result = new Literal(new IntegerLiteral(0));
                foldAssign(instr.getID(), instr.destination(), result);
            }
        }
    }

    @Override
    public void visit(Xor instr) {
        if (instr.leftOperand() instanceof Literal && instr.rightOperand() instanceof Literal) {
            int lVal = ((Literal) instr.leftOperand()).value();
            int rVal = ((Literal) instr.rightOperand()).value();
            Literal result = new Literal(new IntegerLiteral(lVal ^ rVal));
            foldAssign(instr.getID(), instr.destination(), result);
        }
    }

    @Override
    public void visit(Beq instr) {
        // If value is known, remove unreachable code
        if (currBlock != null && instr.comparison() instanceof Literal) {
            int compVal = ((Literal) instr.comparison()).value();
            
            // Remove jump
            instr.eliminate();
            foldRelation(instr, compVal == 0);
            noChanges = false;
        }
    }

    @Override
    public void visit(Bge instr) {
        // If value is known, remove unreachable code
        if (currBlock != null && instr.comparison() instanceof Literal) {
            int compVal = ((Literal) instr.comparison()).value();
            
            // Remove jump
            instr.eliminate();
            foldRelation(instr, compVal >= 0);
            noChanges = false;
        }
    }

    @Override
    public void visit(Bgt instr) {
        // If value is known, remove unreachable code
        if (instr.comparison() instanceof Literal) {
            int compVal = ((Literal) instr.comparison()).value();
            
            // Remove jump
            instr.eliminate();
            foldRelation(instr, compVal > 0);
            noChanges = false;
        }
    }

    @Override
    public void visit(Ble instr) {
        // If value is known, remove unreachable code
        if (currBlock != null && instr.comparison() instanceof Literal) {
            int compVal = ((Literal) instr.comparison()).value();
            
            // Remove jump
            instr.eliminate();
            foldRelation(instr, compVal <= 0);
            noChanges = false;
        }
    }

    @Override
    public void visit(Blt instr) {
        // If value is known, remove unreachable code
        if (currBlock != null && instr.comparison() instanceof Literal) {
            int compVal = ((Literal) instr.comparison()).value();
            
            // Remove jump
            instr.eliminate();
            foldRelation(instr, compVal < 0);
            noChanges = false;
        }
    }

    @Override
    public void visit(Bne instr) {
        // If value is known, remove unreachable code
        if (currBlock != null && instr.comparison() instanceof Literal) {
            int compVal = ((Literal) instr.comparison()).value();
            
            // Remove jump
            instr.eliminate();
            foldRelation(instr, compVal != 0);
            noChanges = false;
        }
    }

    @Override
    public void visit(Bra instr) {}

    @Override
    public void visit(Call instr) {}

    @Override
    public void visit(Return instr) {}

    @Override
    public void visit(Load instr) {}

    @Override
    public void visit(Store instr) {}

    @Override
    public void visit(Read instr) {}

    @Override
    public void visit(ReadB instr) {}

    @Override
    public void visit(Write instr) {}

    @Override
    public void visit(WriteB instr) {}

    @Override
    public void visit(WriteNL instr) {}
}
