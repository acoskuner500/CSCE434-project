package ir.tac;

import ir.cfg.BasicBlock;

public class Bge extends Jump {
   
    public Bge(int id, Value comp, BasicBlock jump, JumpType jType) {
        super(id, comp, jump, jType);
    }

    public Bge(Bge other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.getString("BGE");
    }

    @Override
    public TAC clone() {
        return new Bge(this);
    }
}
