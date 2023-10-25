package ir.cfg;

import ast.*;

public interface CFGVisitor { 

    public void visit(BasicBlock block);
}
