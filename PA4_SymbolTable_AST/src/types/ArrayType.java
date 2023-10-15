package types;

import java.util.List;

public class ArrayType extends Type {
    private Type elemType;
    private List<Integer> dimensions;

    public ArrayType(Type elemType, List<Integer> dimensions) {
        this.elemType = elemType;
        this.dimensions = dimensions;
    }

    public Type elementType() {
        return elemType;
    }

    @Override
    public String toString() {
        String ret = "" + elemType;

        for (Integer i: dimensions) {
            if (i == null) {
                ret += "[]";
            } else {
                ret += "[" + i + "]";
            }
        }

        return ret;
    }
}
