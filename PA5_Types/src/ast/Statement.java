package ast;

public abstract class Statement extends Node {

    // TODO: Add any necessary data members

    protected Statement(int lineNum, int charPos) {
        super(lineNum, charPos);
    }
}
