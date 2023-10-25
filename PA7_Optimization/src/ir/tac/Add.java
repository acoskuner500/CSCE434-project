package ir.tac;

public class Add extends Assign {
    // Either do this way or blend the operator's meaning into Assign
    public Add(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Add");
    }

    @Override
    public String toString() {
        return getString("ADD");
    }
}
