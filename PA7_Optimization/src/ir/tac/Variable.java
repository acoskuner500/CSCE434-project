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

    public boolean isGlobal() {
        return sym.isGlobalVariable();
    }

    public boolean isParameter() {
        return sym.isParameter();
    }

    public boolean isLocal() {
        return sym.isLocal();
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Variable");
    }

    @Override
    public String toString() {
        return sym.name();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Variable) {
            Variable other = (Variable) o;
            return this.sym == other.sym;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return sym.hashCode();
    }
}
