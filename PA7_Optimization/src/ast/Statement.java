package ast;

public abstract class Statement extends Node {

    protected Statement(int lineNum, int charPos) {
        super(lineNum, charPos);
    }
}
