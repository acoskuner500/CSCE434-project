package ast;

public class Multiplication extends Expression {

    private Expression lhs;
    private Expression rhs;
    
    public Multiplication(int lineNum, int charPos, Expression lhs, Expression rhs) {
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
