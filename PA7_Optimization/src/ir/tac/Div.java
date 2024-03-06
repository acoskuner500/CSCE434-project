package ir.tac;

public class Div extends Assign {
    
    public Div(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Div(Div other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("/");
    }

    @Override
    public TAC clone() {
        return new Div(this);
    }
}
