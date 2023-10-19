package ast;

import java.util.ArrayList;
import java.util.Iterator;

public class StatementSequence extends Node implements Iterable<Statement> {
    // Contains a list of statements
    private ArrayList<Statement> stmts;

    public StatementSequence(int lineNum, int charPos, ArrayList<Statement> stmts) {
        super(lineNum, charPos);
        this.stmts = stmts;
    }

    public ArrayList<Statement> statements() {
        return stmts;
    }

    public Iterator<Statement> iterator() {
        return stmts.iterator();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}