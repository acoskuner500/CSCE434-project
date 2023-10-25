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

    public Expression value() {
        return val;
    }

    @Override
    public void accept(TACVisitor visitor) {
        // TODO Auto-generated method stub
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
