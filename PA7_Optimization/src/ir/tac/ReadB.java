package ir.tac;

public class ReadB extends Input {
    
    public ReadB(int id, Variable dest) {
        super(id, dest);
    }

    public ReadB(ReadB other) {
        super(other);
    }

    @Override
    public String toString() {
        return super.getString("READB");
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TAC clone() {
        return new ReadB(this);
    }
}
