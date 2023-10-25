package ir.tac;

import coco.Symbol;
import ir.cfg.BasicBlock;

public class Call extends TAC {
    
    private Symbol function;
    private ValueList args;
    private BasicBlock ref;

    public Call(int id, Symbol function, ValueList args, BasicBlock ref) {
        super(id);
        this.function = function;
        this.args = args;
        this.ref = ref;
    }

    public Symbol function() {
        return function;
    }

    public ValueList arguments() {
        return args;
    }

    public BasicBlock functionBlock() {
        return ref;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Call");
    }

    @Override
    public String toString() {
        String out = super.getID() + " : CALL ";

        for (Value v : args.values()) {
            out += v + " ";
        }

        out += function.name();
        return out;
    }
}
