package ir.tac;

import ir.cfg.BasicBlock;

public class Beq extends TAC {
    
    private Value comp;
    private BasicBlock jump;

    public Beq(int id, Value comp, BasicBlock jump) {
        super(id);
        this.comp = comp;
        this.jump = jump;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Ble");
    }

    @Override
    public String toString() {
        return super.getID() + " : BEQ " + comp + " [" + jump.blockNumber() + "]";
    }
}
