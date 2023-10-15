package ast;

import coco.Symbol;

public abstract class Declaration extends Node {

    private Symbol symbol;

    protected Declaration(int lineNum, int charPos, Symbol symbol) {
        super(lineNum, charPos);
        this.symbol = symbol;
    }

    public Symbol symbol() {
        return symbol;
    }
}
