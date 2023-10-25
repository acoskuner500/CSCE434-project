package ir.tac;

public class Write extends TAC {

    private Value arg;
   
    public Write(int id, Value arg) {
        super(id);
        this.arg = arg;
    }

    public Value argument() {
        return arg;
    }

    @Override
    public String toString() {
        return super.getID() + " : WRITE " + arg;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Write");
    }
}
