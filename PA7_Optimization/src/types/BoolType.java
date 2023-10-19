package types;

public class BoolType extends Type {
    public BoolType() {}

    @Override
    public Type and (Type that) {
        if (that instanceof BoolType) {
            return new BoolType();
        }
        return new ErrorType("Cannot compute " + this + " and " + that + ".");
    }

    @Override
    public Type or (Type that) {
        if (that instanceof BoolType) {
            return new BoolType();
        }
        return new ErrorType("Cannot compute " + this + " or " + that + ".");
    }

    @Override
    public Type not() {
        return new BoolType();
    }

    @Override
    public Type compare (Type that) {
        if (that instanceof BoolType) {
            return new BoolType();
        }
        return new ErrorType("Cannot compare " + this + " with " + that + ".");
    }
    
    @Override
    public String toString() {
        return "bool";
    }
}
