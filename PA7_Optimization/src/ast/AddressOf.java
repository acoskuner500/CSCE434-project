package ast;

import coco.Symbol;
import types.AddressType;

public class AddressOf extends Expression {

    Symbol ident;
    ArrayIndex arrIdx;

    public AddressOf(int lineNum, int charPos, Symbol ident) {
        super(lineNum, charPos);
        this.ident = ident;
        this.arrIdx = null;
    }

    public AddressOf(int lineNum, int charPos, ArrayIndex arrIdx) {
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

    public AddressType type() {
        return new AddressType(ident.type());
    }

    public boolean hasIndex() {
        return arrIdx != null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}