package ast;

public class WhileStatement extends Statement {

    private Expression cond;
    private StatementSequence doStmts;
    
    public WhileStatement(int lineNum, int charPos, Expression cond, StatementSequence doStmts) {
        super(lineNum, charPos);
        this.cond = cond;
        this.doStmts = doStmts;
    }

    public Expression condition() {
        return cond;
    }

    public StatementSequence doStatements() {
        return doStmts;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
