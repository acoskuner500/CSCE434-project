package ir.tac;

public class Pow extends Assign {
    
    public Pow(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Pow");
    }

    @Override
    public String toString() {
        return getString("POW");
    }
}
