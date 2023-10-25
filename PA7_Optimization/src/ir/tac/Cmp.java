package ir.tac;

public class Cmp extends Assign {

    public Cmp(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }
    
    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Cmp");
    }

    @Override
    public String toString() {
        return getString("CMP");
    }
}
