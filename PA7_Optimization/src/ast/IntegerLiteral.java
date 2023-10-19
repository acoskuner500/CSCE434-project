package ast;

public class IntegerLiteral extends Expression {

    int val;
    
    public IntegerLiteral(int lineNum, int charPos, int val) {
        super(lineNum, charPos);
        this.val = val;
    }

    public int value() {
        return val;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
