package ast;

public class Addition extends Expression {

    private Expression lhs;
    private Expression rhs;
   
    public Addition(int lineNum, int charPos, Expression lhs, Expression rhs) {
        super(lineNum, charPos);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Expression leftOperand() {
        return lhs;
    }

    public Expression rightOperand() {
        return rhs;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
