package ir.tac;

public abstract class Assign extends TAC {
    
    private Variable dest; // lhs
    private Value left; // operand_1 
    private Value right; // operand_2

    protected Assign(int id, Variable dest, Value left, Value right) {
        super(id);
        this.dest = dest;
        this.left = left;
        this.right = right;
    }

    public void setDestination(Variable dest) {
        this.dest = dest;
    }

    protected int getID() {
        return super.getID();
    }

    protected Variable destination() {
        return dest;
    }

    protected Value leftOperand() {
        return left;
    }

    protected Value rightOperand() {
        return right;
    }

    protected String getString(String op) {
        return super.getID() + " : " + op + " " + left + " " + (right != null ? right : "");
    }
}
