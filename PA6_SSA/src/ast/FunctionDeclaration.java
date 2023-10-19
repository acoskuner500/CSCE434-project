package ast;

import coco.Symbol;

public class FunctionDeclaration extends Declaration {

    // TODO: Doesn't need parameters?
    private FunctionBody funcBody;
    
    public FunctionDeclaration(int lineNum, int charPos, Symbol symbol, FunctionBody funcBody) {
        super(lineNum, charPos, symbol);
        this.funcBody = funcBody;
    }

    public Symbol function() {
        return super.symbol();
    }

    public FunctionBody body() {
        return funcBody;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
