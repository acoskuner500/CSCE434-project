package ir.tac;

public class Mul extends Assign {
   
    public Mul(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Mul(Mul other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("*");
    }

    @Override
    public TAC clone() {
        return new Mul(this);
    }
}
