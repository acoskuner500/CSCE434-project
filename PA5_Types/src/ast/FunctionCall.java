package ast;

import java.util.List;
import coco.Symbol;

public class FunctionCall extends Expression {
    
    private List<Symbol> overloads;
    private ArgumentList args;

    public FunctionCall(int lineNum, int charPos, List<Symbol> overloads, ArgumentList args) {
        super(lineNum, charPos);
        this.overloads = overloads;
        this.args = args;
    }

    public List<Symbol> symbols() {
        return overloads;
    }

    public boolean noSymbols() {
        return overloads == null;
    }

    public ArgumentList arguments() {
        return args;
    }

    public void resolve(List<Symbol> newSymbol) {
        overloads = newSymbol;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
