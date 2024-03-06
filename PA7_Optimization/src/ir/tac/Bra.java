package ir.tac;

import ir.cfg.BasicBlock;

public class Bra extends TAC {

    private BasicBlock jump;
    
    public Bra(int id, BasicBlock jump) {
        super(id);
        this.jump = jump;
    }

    public Bra(Bra other) {
        super(other);
        this.jump = other.jump;
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    public BasicBlock jumpDestination() {
        return jump;
    }

    public void setJump(BasicBlock newJump) {
        jump = newJump;
    }

    @Override
    public String toString() {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : BRA [" + jump.blockNumber() + "]";
    }

    @Override
    public TAC clone() {
        return new Bra(this);
    }
}
