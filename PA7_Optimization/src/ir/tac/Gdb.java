package ir.tac;

public class Gdb implements Value {

    @Override
    public void accept(TACVisitor visitor) {
        throw new UnsupportedOperationException("Unimlemented method 'accept' for Gdb");
    }

    @Override
    public String toString() {
        return "GDB";
    }
}
