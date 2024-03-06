package ir.tac;

import ast.Expression;
import ast.BoolLiteral;
import ast.IntegerLiteral;
import ast.FloatLiteral;

public class Literal implements Value {

    private Expression val;

    public Literal(Expression val) {
        this.val = val;
    }

    public int value() {
        if (val instanceof IntegerLiteral) {
            return ((IntegerLiteral) val).value();
        } else {
            return ((BoolLiteral) val).value() ? 1 : 0;
        }
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Literal");
    }
    
    @Override
    public String toString () {
        if (val instanceof ast.BoolLiteral) {
            return ((BoolLiteral) val).value().toString();
        }
        else if (val instanceof ast.IntegerLiteral) {
            return ((IntegerLiteral) val).value().toString();
        }
        else if (val instanceof ast.FloatLiteral) {
            return ((FloatLiteral) val).value().toString();
        }
        return "LiteralValueError";
    }
}
