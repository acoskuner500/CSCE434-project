package ir.tac;

import ir.cfg.BasicBlock;

public class Bra extends TAC {

    private BasicBlock jump;
    
    public Bra(int id, BasicBlock jump) {
        super(id);
        this.jump = jump;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Bra");
    }

    @Override
    public String toString() {
        return super.getID() + " : BRA [" + jump.blockNumber() + "]";
    }
}
