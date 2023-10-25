package ast;

public class IntegerLiteral extends Expression {

    Integer val;
    
    public IntegerLiteral(int lineNum, int charPos, Integer val) {
        super(lineNum, charPos);
        this.val = val;
    }

    public Integer value() {
        return val;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
