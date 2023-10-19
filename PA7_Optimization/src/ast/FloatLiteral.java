package ast;

public class FloatLiteral extends Expression {
    
    float val;

    public FloatLiteral(int lineNum, int charPos, float val) {
        super(lineNum, charPos);
        this.val = val;
    }

    public float value() {
        return val;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
