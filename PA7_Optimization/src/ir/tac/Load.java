package ir.tac;

public class Load extends TAC {
   
    private Variable dest;
    private Variable loc;

    public Load(int id, Variable dest, Variable loc) {
        super(id);
        this.dest = dest;
        this.loc = loc;
    }

    public Load(Load other) {
        super(other);
        this.dest = other.dest;
        this.loc = other.loc;
    }

    public Variable destination() {
        return dest;
    }

    public Variable location() {
        return loc;
    }

    public void setDestination(Variable dest) {
        this.dest = dest;
    }

    @Override
    public void accept(TACVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return (super.isEliminated() ? "eliminated-" : "") + super.getID() + " : " + dest + " := LOAD " + loc;
    }

    @Override
    public TAC clone() {
        return new Load(this);
    }
}
