package ast;

import java.util.List;

import coco.Symbol;

public class FunctionDeclaration extends Declaration {

    private FunctionBody funcBody;
    private List<Symbol> params;
    
    public FunctionDeclaration(int lineNum, int charPos, Symbol symbol, FunctionBody funcBody, List<Symbol> params) {
        super(lineNum, charPos, symbol);
        this.funcBody = funcBody;
        this.params = params;
    }

    public Symbol function() {
        return super.symbol();
    }

    public FunctionBody body() {
        return funcBody;
    }

    public List<Symbol> parameters() {
        return params;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
