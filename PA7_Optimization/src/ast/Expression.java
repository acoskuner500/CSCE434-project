package ast;

public abstract class Expression extends Statement {

    // TODO: Add any necessary data members

    protected Expression(int lineNum, int charPos) {
        super(lineNum, charPos);
    }
}
