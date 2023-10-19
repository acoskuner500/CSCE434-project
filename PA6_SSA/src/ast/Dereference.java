package ast;

import coco.Symbol;
import types.Type;

public class Dereference extends Expression { 

    Symbol ident;
    ArrayIndex arrIdx;

    public Dereference(int lineNum, int charPos, Symbol ident) {
        super(lineNum, charPos);
        this.ident = ident;
        this.arrIdx = null;
    }

    public Dereference(int lineNum, int charPos, ArrayIndex arrIdx) {
        super(lineNum, charPos);
        this.ident = null;
        this.arrIdx = arrIdx;
    }

    public Symbol identifier() {
        return ident;
    }

    public ArrayIndex arrayIndex() {
        return arrIdx;
    }

    public Type type() {
        return ident.type();
    }

    public boolean hasIndex() {
        return arrIdx != null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
