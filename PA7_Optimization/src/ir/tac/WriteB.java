package ir.tac;

public class WriteB extends TAC {
   
    private Value arg;

    public WriteB(int id, Value arg) {
        super(id);
        this.arg = arg;
    }

    public Value argument() {
        return arg;
    }

    @Override
    public String toString() {
        return super.getID() + " : WRITEB " + arg;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for WriteB");
    }
}
