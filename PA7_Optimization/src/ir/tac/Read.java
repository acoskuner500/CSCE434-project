package ir.tac;

public class Read extends TAC {
    
    private Variable dest;

    public Read(int id, Variable dest) {
        super(id);
        this.dest = dest;
    }

    public Variable destination() {
        return dest;
    }

    @Override
    public String toString() {
        return super.getID() + " : READ";
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Read");
    }
}
