package coco;

public class Token {

    public enum Kind {
        // boolean operators
        AND("and"),
        OR("or"),
        NOT("not"),

        // arithmetic operators
        POW("^"),

        MUL("*"),
        DIV("/"),
        MOD("%"),

        ADD("+"),
        SUB("-"),

        // relational operators
        EQUAL_TO("=="),
        NOT_EQUAL("!="),
        LESS_THAN("<"),
        LESS_EQUAL("<="),
        GREATER_EQUAL(">="),
        GREATER_THAN(">"),

        // assignment operators
        ASSIGN("="),
        ADD_ASSIGN("+="),
        SUB_ASSIGN("-="),
        MUL_ASSIGN("*="),
        DIV_ASSIGN("/="),
        MOD_ASSIGN("%="),
        POW_ASSIGN("^="),

        // unary increment/decrement
        UNI_INC("++"),
        UNI_DEC("--"),

        // primitive types
        VOID("void"),
        BOOL("bool"),
        INT("int"),
        FLOAT("float"),

        // boolean literals
        TRUE("true"),
        FALSE("false"),

        // region delimiters
        OPEN_PAREN("("),
        CLOSE_PAREN(")"),
        OPEN_BRACE("{"),
        CLOSE_BRACE("}"),
        OPEN_BRACKET("["),
        CLOSE_BRACKET("]"),

        // field/record delimiters
        COMMA(","),
        COLON(":"),
        SEMICOLON(";"),
        PERIOD("."),

        // control flow statements
        IF("if"),
        THEN("then"),
        ELSE("else"),
        FI("fi"),

        WHILE("while"),
        DO("do"),
        OD("od"),

        REPEAT("repeat"),
        UNTIL("until"),

        CALL("call"),
        RETURN("return"),

        // keywords
        MAIN("main"),
        FUNC("function"),

        // special cases
        INT_VAL(),
        FLOAT_VAL(),
        IDENT(),

        EOF(),

        ERROR();

        private String defaultLexeme;

        Kind () {
            defaultLexeme = "";
        }

        Kind (String lexeme) {
            defaultLexeme = lexeme;
        }

        public boolean hasStaticLexeme () {
            return defaultLexeme != null;
        }

        // OPTIONAL: convenience function - boolean matches (String lexeme)
        //           to report whether a Token.Kind has the given lexeme
        //           may be useful
        public boolean matchLexeme (String lexeme) {
            return ( hasStaticLexeme() && defaultLexeme.equals(lexeme) );
        }
    }

    private int lineNum;
    private int charPos;
    Kind kind;  // package-private
    private String lexeme = "";


    // TODO: implement remaining factory functions for handling special cases (EOF below)

    public static Token EOF (int linePos, int charPos) {
        Token tok = new Token(linePos, charPos);
        tok.kind = Kind.EOF;
        return tok;
    }

    private Token (int lineNum, int charPos) {
        this.lineNum = lineNum;
        this.charPos = charPos;

        // no lexeme provide, signal error
        this.kind = Kind.ERROR;
        this.lexeme = "No Lexeme Given";
    }

    public Token (String lexeme, int lineNum, int charPos) {
        this(lineNum, charPos);
        if (lexeme == null) {
            return;
        }
        this.lexeme = lexeme;

        // TODO: based on the given lexeme determine and set the actual kind
        for (Kind kind : Kind.values()) {
            if (kind.matchLexeme(lexeme)) {
                this.kind = kind;
                return;
            }
        }
        State myState = State.Q0;
        for (char c : lexeme.toCharArray()) {
            myState = automaton(myState, c);
        }
        switch (myState) {
            case Q1:
                this.kind = Kind.IDENT;
                return;
            case Q3:
                this.kind = Kind.INT_VAL;
                return;
            case Q5:
                this.kind = Kind.FLOAT_VAL;
                return;
            default:
                break;
        }

        // if we don't match anything, signal error
        this.kind = Kind.ERROR;
        this.lexeme = "Unrecognized lexeme: " + lexeme;
    }

    public int lineNumber () {
        return lineNum;
    }

    public int charPosition () {
        return charPos;
    }

    public String lexeme () {
        // TODO: implement
        return lexeme;
    }

    public Kind kind () {
        // TODO: implement
        return kind;
    }

    // TODO: function to query a token about its kind - boolean is (Token.Kind kind)
    public boolean isKind (Kind kind) {
        return (this.kind == kind);
    }

    // OPTIONAL: add any additional helper or convenience methods
    //           that you find make for a cleaner design

    private enum State {
        Q0, // initial
        Q1, // identifier, accepting
        Q2, // negative
        Q3, // integer val, accepting
        Q4, // decimal point
        Q5, // float val, accepting
        Q6, // invalid
    }

    private State automaton (State currentState, char input) {
        switch (currentState) {
            case Q0:
                if ((input >= 'a' && input <= 'z') || (input >= 'A' && input <= 'Z')) {
                    return State.Q1;
                } else if (input == '-') {
                    return State.Q2;
                } else if (input >= '0' && input <= '9') {
                    return State.Q3;
                } else {
                    return State.Q6;
                }
            case Q1:
                if ((input >= 'a' && input <= 'z') ||
                    (input >= 'A' && input <= 'Z') || 
                    (input >= '0' && input <= '9') ||
                    (input == '_') ) {
                    return State.Q1;
                } else {
                    return State.Q6;
                }
            case Q2:
                if (input >= '0' && input <= '9') {
                    return State.Q3;
                } else {
                    return State.Q6;
                }
            case Q3:
                if (input >= '0' && input <= '9') {
                    return State.Q3;
                } else if (input == '.') {
                    return State.Q4;
                } else {
                    return State.Q6;
                }
            case Q4:
                if (input >= '0' && input <= '9') {
                    return State.Q5;
                } else {
                    return State.Q6;
                }
            case Q5:
                if (input >= '0' && input <= '9') {
                    return State.Q5;
                } else {
                    return State.Q6;
                }
            default:
                return State.Q6;
        }
    }

    @Override
    public String toString () {
        return "Line: " + lineNum + ", Char: " + charPos + ", Lexeme: " + lexeme;
    }
}
