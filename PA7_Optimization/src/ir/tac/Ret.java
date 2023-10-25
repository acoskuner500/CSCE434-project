package ir.tac;

public class Ret extends TAC {
   
    Value retVal;

    public Ret(int id) {
        super(id);
        retVal = null;
    }

    public Ret(int id, Value retVal) {
        super(id);
        this.retVal = retVal;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Ret");
    }

    @Override
    public String toString() {
        return super.getID() + " : RET" + (retVal != null ? " " + retVal : "");
    }
}
