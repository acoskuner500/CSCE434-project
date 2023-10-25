package ir.tac;

public class Mod extends Assign {

    public Mod(int id, Variable dest, Value left, Value right) {
        super(id, dest, left, right);
    }

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'accept' for Mod");
    }    

    @Override
    public String toString() {
        return getString("MOD");
    }
}
