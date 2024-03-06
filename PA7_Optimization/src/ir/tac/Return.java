package ir.tac;

public class Return extends TAC {
    
    private Value retVal;

    public Return(int id) {
        super(id);
        retVal = null;
    }

    public Return(int id, Value retVal) {
        super(id);
        this.retVal = retVal;
    }

    public Return(Return other) {
        super(other);
        this.retVal = other.retVal;
    }

    public Value returnValue() {
        return retVal;
    }

    public boolean hasReturnValue() {
        return retVal != null;
    }

    public void setReturn(Value newVal) {
        retVal = newVal;
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : RET" + (retVal != null ? " " + retVal : "");
    }

    @Override
    public TAC clone() {
        return new Return(this);
    }
}
