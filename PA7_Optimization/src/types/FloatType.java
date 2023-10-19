package types;

public class FloatType extends Type {
    public FloatType() {} 

    @Override
    public Type mul (Type that) {
        if (that instanceof FloatType) {
            return new FloatType();
        }
        return new ErrorType("Cannot multiply " + this + " with " + that + ".");
    }

    @Override
    public Type div (Type that) {
        if (that instanceof FloatType) {
            return new FloatType();
        }
        return new ErrorType("Cannot divide " + this + " by " + that + ".");
    }

    @Override
    public Type add (Type that) {
        if (that instanceof FloatType) {
            return new FloatType();
        }
        return new ErrorType("Cannot add " + this + " to " + that + ".");
    }

    @Override
    public Type sub (Type that) {
        if (that instanceof FloatType) {
            return new FloatType();
        }
        return new ErrorType("Cannot subtract " + that + " from " + this + ".");
    }

    @Override
    public Type mod (Type that) {
        if (that instanceof FloatType) {
            return new FloatType();
        }
        return new ErrorType("Cannot modulo " + that + " by " + this + ".");
    }

    @Override
    public Type pow (Type that) {
        if (that instanceof FloatType) {
            return new FloatType();
        }
        return new ErrorType("Cannot power " + that + " by " + this + ".");
    }

    @Override
    public Type compare (Type that) {
        if (that instanceof FloatType) {
            return new BoolType();
        }
        return new ErrorType("Cannot compare " + this + " with " + that + ".");
    }
    
    @Override
    public String toString() {
        return "float";
    }
}
