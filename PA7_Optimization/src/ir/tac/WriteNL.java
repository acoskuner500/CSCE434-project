package ir.tac;

public class WriteNL extends TAC {

    public WriteNL(int id) {
        super(id);
    }

    @Override
    public String toString() {
        return super.getID() + " : WriteNL";
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for WriteNL");
    }
}
