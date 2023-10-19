package ast;

public class IfStatement extends Statement {

    private Expression cond;
    private StatementSequence thenStmts;
    private StatementSequence elseStmts;

    public IfStatement(int lineNum, int charPos, Expression cond, StatementSequence thenStmts) {
        super(lineNum, charPos);
        this.cond = cond;
        this.thenStmts = thenStmts;
        this.elseStmts = null;
    }

    public IfStatement(int lineNum, int charPos, Expression cond, StatementSequence thenStmts, StatementSequence elseStmts) {
        super(lineNum, charPos);
        this.cond = cond;
        this.thenStmts = thenStmts;
        this.elseStmts = elseStmts;
    }

    public Expression condition() {
        return cond;
    }

    public StatementSequence thenStatements() {
        return thenStmts;
    }

    public StatementSequence elseStatements() {
        return elseStmts;
    }

    public boolean hasElse() {
        return elseStmts != null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
