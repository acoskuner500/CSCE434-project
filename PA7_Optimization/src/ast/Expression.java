package ast;

public abstract class Expression extends Statement {

    protected Expression(int lineNum, int charPos) {
        super(lineNum, charPos);
    }
}
