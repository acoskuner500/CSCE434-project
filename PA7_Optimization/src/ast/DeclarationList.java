package ast;

import java.util.ArrayList;
import java.util.Iterator;

public class DeclarationList extends Node implements Iterable<Declaration>{
    // Contains a list of variable declarations
    private ArrayList<Declaration> decls;

    public DeclarationList(int lineNum, int charPos, ArrayList<Declaration> decls) {
        super(lineNum, charPos);
        this.decls = decls;
    }

    public ArrayList<Declaration> declarations() {
        return decls;
    }

    public boolean empty() {
        return decls == null || decls.isEmpty();
    }

    public Iterator<Declaration> iterator() {
        return decls.iterator();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}