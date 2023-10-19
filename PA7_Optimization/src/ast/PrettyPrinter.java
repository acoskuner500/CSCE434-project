package ast;

import coco.Symbol;

public class PrettyPrinter implements NodeVisitor {

    private int depth = 0;
    private StringBuilder sb = new StringBuilder();

    private void println(Node n, String message) {
        String indent = "";
        for (int i = 0; i < depth; i++) {
            indent += "  ";
        }
        sb.append(indent + n.getClassInfo() + message + "\n");
    }

    private void println(Symbol s) {
        String indent = "";
        for (int i = 0; i < depth; i++) {
            indent += "  ";
        }
        sb.append(indent + s + "\n");
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public void visit(BoolLiteral node) {
        println(node, "[" + node.value() + "]");
    }

    @Override
    public void visit(IntegerLiteral node) {
        println(node, "[" + node.value() + "]");
    }

    @Override
    public void visit(FloatLiteral node) {
        println(node, "[" + node.value() + "]");
    }

    @Override
    public void visit(AddressOf node) {
        println(node, "");
        depth++;
        if (node.hasIndex()) {
            node.arrayIndex().accept(this);
        } else {
            println(node.identifier());
        }
        depth--;
    }

    @Override
    public void visit(ArrayIndex node) {
        println(node, "");
        depth++;
        if (!node.hasSymbol()) {
            node.arrayIndex().accept(this);
        } else {
            println(node.symbol());
        }
        node.indexValue().accept(this);
        depth--;
    }

    @Override
    public void visit(Dereference node) {
        println(node, "");
        depth++;
        if (node.hasIndex()) {
            node.arrayIndex().accept(this);
        } else {
            println(node.identifier());
        }
        depth--;
    }

    @Override
    public void visit(LogicalNot node) {
        println(node, "");
        depth++;
        node.operand().accept(this);
        depth--;
    }

    @Override
    public void visit(Power node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(Multiplication node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(Division node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(Modulo node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(LogicalAnd node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(Addition node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(Subtraction node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(LogicalOr node) {
        println(node, "");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(Relation node) {
        println(node, "[" + node.operator() + "]");
        depth++;
        node.leftOperand().accept(this);
        node.rightOperand().accept(this);
        depth--;
    }

    @Override
    public void visit(Assignment node) {
        println(node, "");
        depth++;
        node.destination().accept(this);
        node.source().accept(this);
        depth--;
    }

    @Override
    public void visit(ArgumentList node) {
        println(node, "");
        if (node.empty())
            return;
        depth++;
        for (Expression e : node) {
            e.accept(this);
        }
        depth--;
    }

    @Override
    public void visit(FunctionCall node) {
        String syms = "[";
        if (!node.noSymbols()) {
            syms += node.symbols().get(0);

            for (int i = 1; i < node.symbols().size(); i++) {
                syms += ", " + node.symbols().get(i);
            }
        }

        syms += "]";
        println(node, syms);
        depth++;
        node.arguments().accept(this);
        depth--;
    }

    @Override
    public void visit(IfStatement node) {
        println(node, "");
        depth++;
        node.condition().accept(this);
        node.thenStatements().accept(this);
        if (node.hasElse()) {
            node.elseStatements().accept(this);
        }
        depth--;
    }

    @Override
    public void visit(WhileStatement node) {
        println(node, "");
        depth++;
        node.condition().accept(this);
        node.doStatements().accept(this);
        depth--;
    }

    @Override
    public void visit(RepeatStatement node) {
        println(node, "");
        depth++;
        node.repeatStatements().accept(this);
        node.condition().accept(this);
        depth--;
    }

    @Override
    public void visit(ReturnStatement node) {
        println(node, "");
        depth++;
        if (node.hasReturn()) {
            node.returnValue().accept(this);
        }
        depth--;
    }

    @Override
    public void visit(StatementSequence node) {
        println(node, "");
        depth++;
        for (Statement s : node) {
            s.accept(this);
        }
        depth--;
    }

    @Override
    public void visit(VariableDeclaration node) {
        println(node, "[" + node.symbol() + "]");
    }

    @Override
    public void visit(FunctionBody node) {
        println(node, "");
        depth++;
        if (node.hasVariables()) {
            node.variables().accept(this);
        }
        node.functionStatementSequence().accept(this);
        depth--;
    }

    @Override
    public void visit(FunctionDeclaration node) {
        println(node, "[" + node.function() + "]");
        depth++;
        node.body().accept(this);
        depth--;
    }

    @Override
    public void visit(DeclarationList node) {
        if (node.empty())
            return;
        println(node, "");
        depth++;
        for (Declaration d : node) {
            d.accept(this);
        }
        depth--;
    }

    @Override
    public void visit(Computation node) {
        println(node, "[" + node.main() + "]");
        depth++;
        node.variables().accept(this);
        node.functions().accept(this);
        node.mainStatementSequence().accept(this);
        depth--;
        // No endline after last node
        sb.deleteCharAt(sb.length() - 1);
    }
}
