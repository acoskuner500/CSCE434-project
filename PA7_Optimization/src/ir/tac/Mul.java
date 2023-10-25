package ir.tac;

public class Mul extends Assign {
   
    public Mul(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Mul");
    }

    @Override
    public String toString() {
        return getString("MUL");
    }
}
