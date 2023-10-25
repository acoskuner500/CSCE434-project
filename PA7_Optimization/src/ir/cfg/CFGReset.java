package ir.cfg;

public class CFGReset implements CFGVisitor {

    @Override
    public void visit(BasicBlock block) {
        block.resetVisited();
        for (Successor s: block.getSuccessors()) {
            s.destination().accept(this, true);
        }
    }

}
