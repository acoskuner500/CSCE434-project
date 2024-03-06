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

    protected Assign(Assign other) {
        super(other);
        this.dest = other.dest;
        this.left = other.left;
        this.right = other.right;
    }

    public void setDestination(Variable dest) {
        this.dest = dest;
    }

    public Variable destination() {
        return dest;
    }

    public Value leftOperand() {
        return left;
    }

    public Value rightOperand() {
        return right;
    }

    public void setLeftOperand(Value newLeft) {
        left = newLeft;
    }

    public void setRightOperand(Value newRight) {
        right = newRight;
    }

    protected String getString(String op) {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : " + dest + " := " + left + " " + (right != null ? op + " " + right : "");
    }
}
