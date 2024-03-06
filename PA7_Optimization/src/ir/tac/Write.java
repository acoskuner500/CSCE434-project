package ir.tac;

public class Write extends Print {

    public Write(int id, Value arg) {
        super(id, arg);
    }

    public Write(Write other) {
        super(other);
    }

    @Override
    public String toString() {
        return super.getString("WRITE");
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TAC clone() {
        return new Write(this);
    }
}
