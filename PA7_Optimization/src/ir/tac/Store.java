package ir.tac;

public class Store extends TAC {
    
    private Value val;
    private Variable loc;

    public Store(int id, Value val, Variable loc) {
        super(id);
        this.val = val;
        this.loc = loc;
    }

    public Store(Store other) {
        super(other);
        this.val = other.val;
        this.loc = other.loc;
    }

    public Variable location() {
        return loc;
    }

    public Value value() {
        return val;
    }

    public void setValue(Value newVal) {
        val = newVal;
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : STORE " + val + " " + loc;
    }

    @Override
    public TAC clone() {
        return new Store(this);
    }
}
