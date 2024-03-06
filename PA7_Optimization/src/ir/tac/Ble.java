package ir.tac;
import ir.cfg.BasicBlock;

public class Ble extends Jump {

    public Ble(int id, Value comp, BasicBlock jump, JumpType jType) {
        super(id, comp, jump, jType);
    }

    public Ble(Ble other) {
        super(other);
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return super.getString("BLE");
    }

    @Override
    public TAC clone() {
        return new Ble(this);
    }
}
