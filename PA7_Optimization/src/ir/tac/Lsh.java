package ir.tac;

public class Lsh extends Assign {
    
    public Lsh(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Lsh(Lsh other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("\\>\\>");
    }

    @Override
    public TAC clone() {
        return new Lsh(this);
    }
}
