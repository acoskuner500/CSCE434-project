package ir.tac;

import coco.Symbol;

public class Temporary extends Variable {

    int instr;
    
    public Temporary(Symbol sym, int instr) {
        super(sym);
        this.instr = instr;
    }

    public int definition() {
        return instr;
    }

    @Override
    public String toString() {
        return "(" + instr + ")";
    }
}
