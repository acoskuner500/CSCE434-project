package ir.tac;

public class Sub extends Assign {
    
    public Sub(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }
    
    public Sub(Sub other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("-");
    }

    @Override
    public TAC clone() {
        return new Sub(this);
    }
}
