package ir.tac;

public class Div extends Assign {
    
    public Div(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Div");
    }

    @Override
    public String toString() {
        return getString("DIV");
    }
}
