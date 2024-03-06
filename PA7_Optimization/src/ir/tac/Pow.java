package ir.tac;

public class Pow extends Assign {
    
    public Pow(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Pow(Pow other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("^");
    }

    @Override
    public TAC clone() {
        return new Pow(this);
    }
}
