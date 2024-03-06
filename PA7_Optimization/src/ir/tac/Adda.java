package ir.tac;

public class Adda extends Assign {
    
    public Adda(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Adda(Adda other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: Print this better
        return getString("ADDA");
    }

    @Override
    public TAC clone() {
        return new Adda(this);
    }
}
