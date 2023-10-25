package ir.tac;

public class Load extends TAC {
   
    private Variable dest;
    private Variable loc;

    public Load(int id, Variable dest, Variable loc) {
        super(id);
        this.dest = dest;
        this.loc = loc;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Load");
    }

    @Override
    public String toString() {
        return super.getID() + " : LOAD " + loc;
    }
}
