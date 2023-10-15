package ast;

public class FunctionBody extends Node {

    private DeclarationList vars;
    private StatementSequence funcSeq;
    
    public FunctionBody(int lineNum, int charPos, DeclarationList vars, StatementSequence funcSeq) {
        super(lineNum, charPos);
        this.vars = vars;
        this.funcSeq = funcSeq;
    }

    public DeclarationList variables() {
        return vars;
    }

    public StatementSequence functionStatementSequence() {
        return funcSeq;
    }

    public boolean hasVariables() {
        return vars != null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
