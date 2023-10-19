package ast;

public class Relation extends Expression {

    private Expression lhs;
    private String op;
    private Expression rhs;
    
    public Relation(int lineNum, int charPos, Expression lhs, String op, Expression rhs) {
        super(lineNum, charPos);
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public Expression leftOperand() {
        return lhs;
    }

    public String operator() {
        return op;
    }

    public Expression rightOperand() {
        return rhs;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
