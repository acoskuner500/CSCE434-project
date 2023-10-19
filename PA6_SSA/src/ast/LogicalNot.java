package ast;

public class LogicalNot extends Expression {

    Expression expr;
    
    public LogicalNot(int lineNum, int charPos, Expression expr) {
        super(lineNum, charPos);
        this.expr = expr;
    }

    public Expression operand() {
        return expr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
