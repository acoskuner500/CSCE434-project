package ir.tac;

import ir.cfg.BasicBlock;

public class Bne extends TAC { 

    private Value comp;
    private BasicBlock jump;

    public Bne(int id, Value comp, BasicBlock jump) {
        super(id);
        this.comp = comp;
        this.jump = jump;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Bne");
    }

    @Override
    public String toString() {
        return super.getID() + " : BNE " + comp + " [" + jump.blockNumber() + "]";
    }
}
