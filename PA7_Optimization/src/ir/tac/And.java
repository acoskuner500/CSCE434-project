package ir.tac;

public class And extends Assign {
   
    public And(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for And");
    }

    @Override
    public String toString() {
        return getString("AND");
    }
}
