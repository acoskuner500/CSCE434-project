package types;

public class AddressType extends Type {
    private Type elemType;
    
    public AddressType(Type elemType) {
        this.elemType = elemType;
    }

    public Type elementType() {
        return elemType;
    }

    public Type assign (Type source) {
        if (elemType.getClass() == source.getClass()) {
            // If array, need to have same dimensions
            if (elemType instanceof ArrayType) {
                ArrayType one = (ArrayType) elemType;
                ArrayType two = (ArrayType) source;

                while (one.elementType().getClass() == two.elementType().getClass()) {
                    if (one.elementType() instanceof ArrayType) {
                        one = (ArrayType) one.elementType();
                        two = (ArrayType) two.elementType();
                    } else {
                        return new VoidType();
                    }
                }
                return new ErrorType("Cannot assign " + source + " to " + this + ".");
            }
            return new VoidType();
        }

        return new ErrorType("Cannot assign " + source + " to " + this + ".");
    }

    @Override
    public String toString() {
        return "AddressOf(" + elemType + ")";
    }
}
