package ir.tac;

public class WriteB extends Print {
   
    public WriteB(int id, Value arg) {
        super(id, arg);
    }

    public WriteB(WriteB other) {
        super(other);
    }

    @Override
    public String toString() {
        return super.getString("WRITEB");
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TAC clone() {
        return new WriteB(this);
    }
}
