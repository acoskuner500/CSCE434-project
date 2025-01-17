package ast;

public class BoolLiteral extends Expression {

    Boolean val;

    public BoolLiteral(Boolean val) {
        super(0, 0);
        this.val = val;
    }
    
    public BoolLiteral(int lineNum, int charPos, Boolean val) {
        super(lineNum, charPos);
        this.val = val;
    }

    public Boolean value() {
        return val;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
