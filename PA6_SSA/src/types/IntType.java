package types;

public class IntType extends Type {
    public IntType() {}

    @Override
    public Type mul (Type that) {
        if (that instanceof IntType) {
            return new IntType();
        }
        return new ErrorType("Cannot multiply " + this + " with " + that + ".");
    }

    @Override
    public Type div (Type that) {
        if (that instanceof IntType) {
            return new IntType();
        }
        return new ErrorType("Cannot divide " + this + " by " + that + ".");
    }

    @Override
    public Type add (Type that) {
        if (that instanceof IntType) {
            return new IntType();
        }
        return new ErrorType("Cannot add " + this + " to " + that + ".");
    }

    @Override
    public Type sub (Type that) {
        if (that instanceof IntType) {
            return new IntType();
        }
        return new ErrorType("Cannot subtract " + that + " from " + this + ".");
    }

    @Override
    public Type mod (Type that) {
        if (that instanceof IntType) {
            return new IntType();
        }
        return new ErrorType("Cannot modulo " + this + " by " + that + ".");
    }

    @Override
    public Type pow (Type that) {
        if (that instanceof IntType) {
            return new IntType();
        }
        return new ErrorType("Cannot power " + this + " by " + that + ".");
    }

    @Override
    public Type compare (Type that) {
        if (that instanceof IntType) {
            return new BoolType();
        }
        return new ErrorType("Cannot compare " + this + " with " + that + ".");
    }
    
    @Override
    public String toString() {
        return "int";
    }
}
