package ir.tac;

import coco.Symbol;

public class Variable implements Value {

    private Symbol sym;

    public Variable(Symbol sym) {
        this.sym = sym;
    }

    public Symbol symbol() {
        return sym;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Variable");
    }

    @Override
    public String toString() {
        return sym.name();
    }
}
