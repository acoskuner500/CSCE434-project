package ast;

import coco.Symbol;

public class ArrayIndex extends Node {
    
    private ArrayIndex arrIdx;
    private Symbol ident;
    private Expression idx;

    
    public ArrayIndex(int lineNum, int charPos, ArrayIndex arrIdx, Expression idx) {
        super(lineNum, charPos);
        this.arrIdx = arrIdx;
        this.idx = idx;
        this.ident = null;
    }

    public ArrayIndex(int lineNum, int charPos, Symbol ident, Expression idx) {
        super(lineNum, charPos);
        this.ident = ident;
        this.idx = idx;
        this.arrIdx = null;
    }

    public ArrayIndex arrayIndex() {
        return arrIdx;
    }

    public Symbol symbol() {
        return ident;
    }

    public Expression indexValue() {
        return idx;
    }

    public boolean hasSymbol() {
        return ident != null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
