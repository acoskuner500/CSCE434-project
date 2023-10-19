package ast;

public class AST {

    private Computation root;

    // TODO: Create AST structure of your choice
    public AST(Computation root) {
        this.root = root;
    }

    public Computation getRoot() {
        return root;
    }

    public String printPreOrder(){
        // TODO: Return the pre order traversal of AST. Use "\n" as separator.
        // Use the enum ASTNonTerminal provided for naming convention.
        PrettyPrinter printer = new PrettyPrinter();
        if (root != null) {
            printer.visit(root);
            return printer.toString();
        }
        return "";
    }
}
