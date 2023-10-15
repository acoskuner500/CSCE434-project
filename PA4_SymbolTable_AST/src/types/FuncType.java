package types;

public class FuncType extends Type {

    private TypeList params;
    private Type retType;

    public FuncType(TypeList params, Type retType) {
        this.params = params;
        this.retType = retType;
    }

    public TypeList parameters() {
        return params;
    }

    public Type returnType() {
        return retType;
    }

    @Override
    public String toString() {
        if (params == null) {
            return "()->" + retType;
        }
        return "(" + params + ")->" + retType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof FuncType)) {
            return false;
        }

        FuncType t = (FuncType) o;

        // They are equal if parameters are equal (return type doesn't matter)
        return params.equals(t.params);
    }
}
