package ir.tac;

import ir.cfg.BasicBlock;

public class Blt extends TAC {
    
    private Value comp;
    private BasicBlock jump;

    public Blt(int id, Value comp, BasicBlock jump) {
        super(id);
        this.comp = comp;
        this.jump = jump;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Blt");
    }

    @Override
    public String toString() {
        return super.getID() + " : BLT " + comp + " [" + jump.blockNumber() + "]";
    }
}
