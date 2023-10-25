package ir.tac;

import ir.cfg.BasicBlock;

public class Bgt extends TAC {
   
    private Value comp;
    private BasicBlock jump;

    public Bgt(int id, Value comp, BasicBlock jump) {
        super(id);
        this.comp = comp;
        this.jump = jump;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Bgt");
    }

    @Override
    public String toString() {
        return super.getID() + " : BGT " + comp + " [" + jump.blockNumber() + "]";
    }
}
