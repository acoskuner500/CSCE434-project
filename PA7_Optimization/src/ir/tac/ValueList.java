package ir.tac;

import java.util.List;

public class ValueList implements Visitable {
   
    List<Value> vals;

    public ValueList(List<Value> vals) {
        this.vals = vals;
    }

    public List<Value> values() {
        return vals;
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for ValueList");
    }
}
