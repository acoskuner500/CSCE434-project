package ast;

public class BoolLiteral extends Expression {

    boolean val;
    
    public BoolLiteral(int lineNum, int charPos, boolean val) {
        super(lineNum, charPos);
        this.val = val;
    }

    public boolean value() {
        return val;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
