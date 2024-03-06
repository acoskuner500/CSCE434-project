package ir.tac;

public class Read extends Input {
    
    public Read(int id, Variable dest) {
        super(id, dest);
    }

    public Read(Read other) {
        super(other);
    }

    @Override
    public String toString() {
        return super.getString("READ");
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TAC clone() {
        return new Read(this);
    }
}
