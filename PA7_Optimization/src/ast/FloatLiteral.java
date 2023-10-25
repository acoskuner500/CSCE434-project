package ast;

public class FloatLiteral extends Expression {
    
    Float val;

    public FloatLiteral(int lineNum, int charPos, Float val) {
        super(lineNum, charPos);
        this.val = val;
    }

    public Float value() {
        return val;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
