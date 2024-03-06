package ir.tac;

import ir.cfg.BasicBlock;

public class Bgt extends Jump {

    public Bgt(int id, Value comp, BasicBlock jump, JumpType jType) {
        super(id, comp, jump, jType);
    }

    public Bgt(Bgt other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.getString("BGT");
    }

    @Override
    public TAC clone() {
        return new Bgt(this);
    }
}
