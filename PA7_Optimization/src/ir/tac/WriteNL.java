package ir.tac;

public class WriteNL extends Print {

    public WriteNL(int id) {
        super(id, null);
    }

    public WriteNL(WriteNL other) {
        super(other);
    }

    @Override
    public String toString() {
        return super.getString("WriteNL");
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TAC clone() {
        return new WriteNL(this);
    }
}
