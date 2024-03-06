package ir.tac;

public class Move extends Assign {
    
    public Move(int id, Variable dest, Value source) {
        super(id, dest, source, null);
    }

    public Move(Move other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getString(null);
    }

    @Override
    public TAC clone() {
        return new Move(this);
    }
}
