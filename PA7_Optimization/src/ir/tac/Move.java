package ir.tac;

public class Move extends Assign {
    
    public Move(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Move");
    }

    @Override
    public String toString() {
        return getString("MOVE");
    }
}
