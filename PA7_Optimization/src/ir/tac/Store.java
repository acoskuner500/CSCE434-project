package ir.tac;

public class Store extends TAC {
    
    private Variable loc;
    private Value val;

    public Store(int id, Variable loc, Value val) {
        super(id);
        this.loc = loc;
        this.val = val;
    }

    public Variable location() {
        return loc;
    }

    public Value value() {
        return val;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Store");
    }

    @Override
    public String toString() {
        return super.getID() + " : STORE " + val + " " + loc;
    }
}
