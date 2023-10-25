package ir.tac;

public class Return extends TAC {
    
    private Variable var;

    public Return(int id, Variable var) {
        super(id);
        this.var = var;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Return");
    }
}
