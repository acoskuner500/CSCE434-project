package ir.tac;

public class ReadB extends TAC {
    
    private Variable dest;

    public ReadB(int id, Variable dest) {
        super(id);
        this.dest = dest;
    }

    public Variable destination() {
        return dest;
    }

    @Override
    public String toString() {
        return super.getID() + " : READB";
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for ReadB");
    }
}
