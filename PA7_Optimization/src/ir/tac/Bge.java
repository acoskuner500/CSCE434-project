package ir.tac;

import ir.cfg.BasicBlock;

public class Bge extends TAC {
   
    private Value comp;
    private BasicBlock jump;

    public Bge(int id, Value comp, BasicBlock jump) {
        super(id);
        this.comp = comp;
        this.jump = jump;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Bge");
    }

    @Override
    public String toString() {
        return super.getID() + " : BGE " + comp + " [" + jump.blockNumber() + "]";
    }
}
