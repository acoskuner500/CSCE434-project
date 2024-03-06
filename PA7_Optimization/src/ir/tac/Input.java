package ir.tac;

public abstract class Input extends TAC {

    private Variable dest;

    protected Input(int id, Variable dest) {
        super(id);
        this.dest = dest;
    }

    protected Input(Input other) {
        super(other);
        this.dest = other.dest;
    }

    public Variable destination() {
        return dest;
    }

    public void setDestination(Variable dest) {
        this.dest = dest;
    }

    public String getString(String op) {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : " + dest + " = " + op;
    }
}
