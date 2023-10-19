package ast;

public class RepeatStatement extends Statement {

    private StatementSequence repStmts;
    private Expression cond;
   
    public RepeatStatement(int lineNum, int charPos, StatementSequence repStmts, Expression cond) {
        super(lineNum, charPos);
        this.repStmts = repStmts;
        this.cond = cond;
    }

    public StatementSequence repeatStatements() {
        return repStmts;
    }

    public Expression condition() {
        return cond;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
