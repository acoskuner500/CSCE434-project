package ast;

import coco.Symbol;

public class VariableDeclaration extends Declaration {

    public VariableDeclaration(int lineNum, int charPos, Symbol symbol) {
        super(lineNum, charPos, symbol);
    }

    public Symbol symbol() {
        return super.symbol();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
