package ast;

public class Assignment extends Statement {
    // Left side can be variable or array access
    Expression lhs;
    // Right side can be any literal or expression
    Expression rhs;
    
    public Assignment(int lineNum, int charPos, Expression lhs, Expression rhs) {
        super(lineNum, charPos);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Expression destination() {
        return lhs;
    }

    public Expression source() {
        return rhs;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
