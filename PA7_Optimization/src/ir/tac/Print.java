package ir.tac;

public abstract class Print extends TAC {
    
    private Value arg;

    protected Print(int id, Value arg) {
        super(id);
        this.arg = arg;
    }

    protected Print(Print other) {
        super(other);
        this.arg = other.arg;
    }

    public Value argument() {
        return arg;
    }

    public void setArgument(Value newArg) {
        arg = newArg;
    }

    public String getString(String op) {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : " + op + (arg != null ? " " + arg : "");
    }
}
