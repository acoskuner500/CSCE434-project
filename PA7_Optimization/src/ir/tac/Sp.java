package ir.tac;

public class Sp implements Value {
    
    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimlemented method 'accept' for Sp");
    }

    @Override
    public String toString() {
        return "SP";
    }
}
