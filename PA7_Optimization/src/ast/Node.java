package ast;

import coco.Token;

public abstract class Node implements Visitable {

    private int lineNum;
    private int charPos;

    protected Node (int lineNum, int charPos) {
        this.lineNum = lineNum;
        this.charPos = charPos;
    }

    public int lineNumber () {
        return lineNum;
    }

    public int charPosition () {
        return charPos;
    }

    public String getClassInfo () {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString () {
        return this.getClass().getSimpleName();
    }

    // Some factory method
    public static Statement newAssignment (int lineNum, int charPos, Expression dest, Token assignOp, Expression src) {
        switch (assignOp.kind()) {
            case ADD_ASSIGN:
                return new Assignment(lineNum, charPos, dest, new Addition(lineNum, charPos, dest, src));
            case SUB_ASSIGN:
                return new Assignment(lineNum, charPos, dest, new Subtraction(lineNum, charPos, dest, src));
            case MUL_ASSIGN:
                return new Assignment(lineNum, charPos, dest, new Multiplication(lineNum, charPos, dest, src));
            case DIV_ASSIGN:
                return new Assignment(lineNum, charPos, dest, new Division(lineNum, charPos, dest, src));
            case MOD_ASSIGN:
                return new Assignment(lineNum, charPos, dest, new Modulo(lineNum, charPos, dest, src));
            case POW_ASSIGN:
                return new Assignment(lineNum, charPos, dest, new Power(lineNum, charPos, dest, src));
            default:
                return new Assignment(lineNum, charPos, dest, src);
        }
    }

    // public static Expression newExpression (Expression leftSide, Token op, Expression rightSide) {

    // }

    // public static Expression newLiteral (Token tok) {

    // }
}
