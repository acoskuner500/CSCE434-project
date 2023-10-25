package ir.tac;

public class Not extends Assign {
    
    public Not(int id, Variable dest, Value val) {
        super(id, dest, val, null);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Not");
    }

    @Override
    public String toString() {
        return getString("NOT");
    }
}
