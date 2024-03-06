package ir.tac;

import ir.cfg.BasicBlock;

public abstract class Jump extends TAC {

    public enum JumpType {
        IF_THEN,
        IF_ELSE,
        WHILE,
        REPEAT,
    };
   
    private Value comp;
    private BasicBlock jumpDest;
    private JumpType jType; 

    protected Jump(int id, Value comp, BasicBlock jumpDest, JumpType jType) {
        super(id);
        this.comp = comp;
        this.jumpDest = jumpDest;
        this.jType = jType;
    }

    protected Jump(Jump other) {
        super(other);
        this.comp = other.comp;
        this.jumpDest = other.jumpDest;
    }

    public Value comparison() {
        return comp;
    }

    public BasicBlock jumpDestination() {
        return jumpDest;
    }

    public JumpType jumpType() {
        return jType;
    }

    public void setComparison(Value newComp) {
        comp = newComp;
    }

    public void setJumpDestination(BasicBlock newDest) {
        jumpDest = newDest;
    }

    protected String getString(String op) {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : " + op + " " + comp + " [" + jumpDest.blockNumber() + "]";
    }
}
