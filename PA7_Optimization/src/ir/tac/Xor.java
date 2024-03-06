package ir.tac;

public class Xor extends Assign {
   
    public Xor(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Xor(Xor other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("xor");
    }

    @Override
    public TAC clone() {
        return new Xor(this);
    }
}
