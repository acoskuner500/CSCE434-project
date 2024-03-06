package ir.tac;

import ir.cfg.BasicBlock;

public class Blt extends Jump {
    
    public Blt(int id, Value comp, BasicBlock jump, JumpType jType) {
        super(id, comp, jump, jType);
    }

    public Blt(Blt other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.getString("BLT");
    }

    @Override
    public TAC clone() {
        return new Blt(this);
    }
}
