package ir.tac;

public class Or extends Assign {
    
    public Or(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Or");
    }

    @Override
    public String toString() {
        return getString("OR");
    }
}
