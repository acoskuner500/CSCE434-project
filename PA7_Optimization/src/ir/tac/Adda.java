package ir.tac;

public class Adda extends Assign {
    
    public Adda(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Adda");
    }

    @Override
    public String toString() {
        return getString("ADDA");
    }
}
