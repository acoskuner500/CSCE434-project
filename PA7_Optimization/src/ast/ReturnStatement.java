package ast;

public class ReturnStatement extends Statement {

    private Expression retVal;

    public ReturnStatement(int lineNum, int charPos) {
        super(lineNum, charPos);
        this.retVal = null;
    }

    public ReturnStatement(int lineNum, int charPos, Expression retVal) {
        super(lineNum, charPos);
        this.retVal = retVal;
    }

    public Expression returnValue() {
        return retVal;
    }

    public boolean hasReturn() {
        return retVal != null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
