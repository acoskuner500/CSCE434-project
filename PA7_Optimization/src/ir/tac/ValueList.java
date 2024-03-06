package ir.tac;

import java.util.List;
import java.util.Iterator;

public class ValueList implements Visitable, Iterable<Value> {
   
    List<Value> vals;

    public ValueList(List<Value> vals) {
        this.vals = vals;
    }

    public List<Value> values() {
        return vals;
    }

    @Override
    public Iterator<Value> iterator() {
        return vals.iterator();
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for ValueList");
    }
}
