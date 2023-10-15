package types;

public class ArrayType extends Type {
    private Type elemType;
    private Integer numElems;

    public ArrayType(Type elemType, Integer numElems) {
        this.elemType = elemType;
        this.numElems = numElems;
    }

    public Type elementType() {
        return elemType;
    }

    public Integer numElements() {
        return numElems;
    }

    @Override
    public String toString() {
        if (numElems == null) {
            return "" + elemType + "[]";
        }

        if (elemType instanceof ArrayType) {
            String s = "" + elemType;
            int i;
            for (i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '[') {
                    break;
                }
            }
            return s.substring(0, i) + "[" + numElems + "]" + s.substring(i);
        }

        return "" + elemType + "[" + numElems + "]";
    }
}
