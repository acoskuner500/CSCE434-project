package ir.tac;

public class Sub extends Assign {
    
    public Sub(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Sub");
    }

    @Override
    public String toString() {
        return getString("SUB");
    }
}
