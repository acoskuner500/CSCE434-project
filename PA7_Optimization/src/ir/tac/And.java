package ir.tac;

public class And extends Assign {
   
    public And(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public And(And other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("and");
    }

    @Override
    public TAC clone() {
        return new And(this);
    }
}
