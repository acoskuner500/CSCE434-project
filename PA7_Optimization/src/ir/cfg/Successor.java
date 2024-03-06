package ir.cfg;

public class Successor {
    
    private BasicBlock dest;

    // These are for DOT graph configurations
    private String label;
    private String arrowType;

    public Successor(BasicBlock dest) {
        this.dest = dest;
        this.label = "";
        this.arrowType = "n";
    }

    public Successor(BasicBlock dest, String label) {
        this.dest = dest;
        this.label = label;
        this.arrowType = "n";
    }

    public Successor(BasicBlock dest, String label, String arrowType) {
        this.dest = dest;
        this.label = label;
        this.arrowType = arrowType;
    }
    
    public BasicBlock destination() {
        return dest;
    }

    public String label() {
        return label;
    }

    public String arrowType() {
        return arrowType;
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }
}
