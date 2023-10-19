package ast;

import java.util.ArrayList;
import java.util.Iterator;

public class ArgumentList extends Node implements Iterable<Expression> {
    // Contains a list of passed parameter expressions
    private ArrayList<Expression> params;

    public ArgumentList(int lineNum, int charPos, ArrayList<Expression> params) {
        super(lineNum, charPos);
        this.params = params;
    }

    public ArrayList<Expression> parameters() {
        return params;
    }

    public boolean empty() {
        return params.isEmpty();
    }

    public Iterator<Expression> iterator() {
        return params.iterator();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
