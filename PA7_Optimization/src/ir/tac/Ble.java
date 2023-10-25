package ir.tac;
import ir.cfg.BasicBlock;

public class Ble extends TAC {

    private Value comp;
    private BasicBlock jump;
    
    public Ble(int id, Value comp, BasicBlock jump) {
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
        return super.getID() + " : BLE " + comp + " [" + jump.blockNumber() + "]";
    }
}
