package ir.tac;

import ir.cfg.BasicBlock;

public class Beq extends Jump {
    
    public Beq(int id, Value comp, BasicBlock jump, JumpType jType) {
        super(id, comp, jump, jType);
    }

    public Beq(Beq other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.getString("BEQ");
    }

    @Override
    public TAC clone() {
        return new Beq(this);
    }
}
