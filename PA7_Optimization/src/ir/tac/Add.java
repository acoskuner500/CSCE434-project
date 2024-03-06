package ir.tac;

public class Add extends Assign {
    // Either do this way or blend the operator's meaning into Assign
    public Add(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Add(Add other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("+");
    }

    @Override
    public TAC clone() {
        return new Add(this);
    }
}
