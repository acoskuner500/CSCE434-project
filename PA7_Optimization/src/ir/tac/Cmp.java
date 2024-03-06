package ir.tac;

public class Cmp extends Assign {

    public Cmp(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    public Cmp(Cmp other) {
        super(other);
    }
    
    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString("CMP");
    }

    @Override
    public TAC clone() {
        return new Cmp(this);
    }
}
