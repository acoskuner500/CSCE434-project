package ir.tac;

import coco.Symbol;
import ir.cfg.CFG;

public class Call extends TAC {
    
    private Symbol function;
    private ValueList args;
    private CFG ref;
    private Variable dest;

    public Call(int id, Symbol function, ValueList args, CFG ref) {
        super(id);
        this.function = function;
        this.args = args;
        this.ref = ref;
        this.dest = null;
    }

    public Call(int id, Symbol function, ValueList args, CFG ref, Variable dest) {
        super(id);
        this.function = function;
        this.args = args;
        this.ref = ref;
        this.dest = dest;
    }

    public Call(Call other) {
        super(other);
        this.function = other.function();
        this.args = other.args;
        this.ref = other.ref;
        this.dest = other.dest;
    }

    public Symbol function() {
        return function;
    }

    public ValueList arguments() {
        return args;
    }

    public CFG functionCFG() {
        return ref;
    }

    public Variable destination() {
        return dest;
    }

    public void setFunctionCFG(CFG funcCFG) {
        ref = funcCFG;
    }

    public void setDestination(Variable dest) {
        this.dest = dest;
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String out = (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : " + (dest != null ? dest + " = " : "" ) + "CALL " + function.name() + "(";

        for (int i = 0; i < args.values().size(); i++) {
            out += args.values().get(i);

            if (i < args.values().size() - 1) {
                out += ", ";
            }
        }

        out += ")";
        return out;
    }

    @Override
    public TAC clone() {
        return new Call(this);
    }
}
