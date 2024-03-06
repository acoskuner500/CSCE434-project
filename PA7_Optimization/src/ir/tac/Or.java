package ir.tac;

public class Or extends Assign {
    
    public Or(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Or(Or other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("or");
    }

    @Override
    public TAC clone() {
        return new Or(this);
    }
}
