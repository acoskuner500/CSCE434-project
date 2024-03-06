package ir.tac;

import ir.cfg.BasicBlock;

public class Bne extends Jump { 

    public Bne(int id, Value comp, BasicBlock jump, JumpType jType) {
        super(id, comp, jump, jType);
    }

    public Bne(Bne other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.getString("BNE");
    }

    @Override
    public TAC clone() {
        return new Bne(this);
    }
}
