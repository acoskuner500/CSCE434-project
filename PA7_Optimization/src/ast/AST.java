package ast;

public class AST {

    private Computation root;

    public AST(Computation root) {
        this.root = root;
    }

    public Computation getRoot() {
        return root;
    }

    public String printPreOrder(){
        // Use the enum ASTNonTerminal provided for naming convention.
        PrettyPrinter printer = new PrettyPrinter();
        if (root != null) {
            printer.visit(root);
            return printer.toString();
        }
        return "";
    }

    // TODO: Can represent AST as dot graph, but not necessary
    public String asDotGraph() {
        return "";
    }
}
