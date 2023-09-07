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
    private State currentState;


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
        this.lexeme = lexeme;

        // TODO: based on the given lexeme determine and set the actual kind
        currentState = State.Q0;
        for (char c : lexeme.toCharArray()) {
            automaton(c);
        }
        this.kind = acceptingState();
        if (currentState != State.Q115) return;

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

    // Function to return Kind based on accepting states
    private Kind acceptingState() {
        switch (currentState) {
            case Q3:    return Kind.AND;
            case Q7:    return Kind.BOOL;
            case Q11:   return Kind.CALL;
            case Q13:   return Kind.DO;
            case Q17:   return Kind.ELSE;
            case Q22:   return Kind.FALSE;
            case Q23:   return Kind.FI;
            case Q27:   return Kind.FLOAT;
            case Q34:   return Kind.FUNC;
            case Q36:   return Kind.IF;
            case Q38:   return Kind.INT;
            case Q42:   return Kind.MAIN;
            case Q45:   return Kind.NOT;
            case Q47:   return Kind.OD;
            case Q48:   return Kind.OR;
            case Q54:   return Kind.REPEAT;
            case Q58:   return Kind.RETURN;
            case Q62:   return Kind.THEN;
            case Q65:   return Kind.TRUE;
            case Q70:   return Kind.UNTIL;
            case Q74:   return Kind.VOID;
            case Q79:   return Kind.WHILE;
            case Q81:   return Kind.POW;
            case Q82:   return Kind.POW_ASSIGN;
            case Q83:   return Kind.MUL;
            case Q84:   return Kind.MUL_ASSIGN;
            case Q85:   return Kind.DIV;
            case Q86:   return Kind.DIV_ASSIGN;
            case Q87:   return Kind.MOD;
            case Q88:   return Kind.MOD_ASSIGN;
            case Q89:   return Kind.ADD;
            case Q90:   return Kind.ADD_ASSIGN;
            case Q91:   return Kind.SUB;
            case Q92:   return Kind.SUB_ASSIGN;
            case Q93:   return Kind.ASSIGN;
            case Q94:   return Kind.EQUAL_TO;
            case Q96:   return Kind.NOT_EQUAL;
            case Q97:   return Kind.LESS_THAN;
            case Q98:   return Kind.LESS_EQUAL;
            case Q99:   return Kind.GREATER_THAN;
            case Q100:  return Kind.GREATER_EQUAL;
            case Q101:  return Kind.UNI_INC;
            case Q102:  return Kind.UNI_DEC;
            case Q103:  return Kind.COMMA;
            case Q104:  return Kind.COLON;
            case Q105:  return Kind.SEMICOLON;
            case Q106:  return Kind.PERIOD;
            case Q107:  return Kind.OPEN_PAREN;
            case Q108:  return Kind.CLOSE_PAREN;
            case Q109:  return Kind.OPEN_BRACE;
            case Q110:  return Kind.CLOSE_BRACE;
            case Q111:  return Kind.OPEN_BRACKET;
            case Q112:  return Kind.CLOSE_BRACKET;
            case Q80:   return Kind.IDENT;
            case Q113:  return Kind.INT_VAL;
            case Q114:  return Kind.FLOAT_VAL;
            default:    return Kind.ERROR;
        }
    }

    // All states (accepting, intermediate, and invalid)
    private enum State {
        Q0,
        Q1,
        Q2,
        Q3, // and
        Q4,
        Q5,
        Q6,
        Q7, // bool
        Q8,
        Q9,
        Q10,
        Q11, // call
        Q12,
        Q13, // do
        Q14,
        Q15,
        Q16,
        Q17, // else
        Q18,
        Q19,
        Q20,
        Q21,
        Q22, // false
        Q23, // fi
        Q24,
        Q25,
        Q26,
        Q27, // float
        Q28,
        Q29,
        Q30,
        Q31,
        Q32,
        Q33,
        Q34, // function
        Q35,
        Q36, // if
        Q37,
        Q38, // int
        Q39,
        Q40,
        Q41,
        Q42, // main
        Q43,
        Q44,
        Q45, // not
        Q46,
        Q47, // od
        Q48, // or
        Q49,
        Q50,
        Q51,
        Q52,
        Q53,
        Q54, // repeat
        Q55,
        Q56,
        Q57,
        Q58, // return
        Q59,
        Q60,
        Q61,
        Q62, // then
        Q63,
        Q64,
        Q65, // true
        Q66,
        Q67,
        Q68,
        Q69,
        Q70, // until
        Q71,
        Q72,
        Q73,
        Q74, // void
        Q75,
        Q76,
        Q77,
        Q78,
        Q79, // while
        Q80, // identifier
        Q81, // ^
        Q82, // ^=
        Q83, // *
        Q84, // *=
        Q85, // /
        Q86, // /=
        Q87, // %
        Q88, // %=
        Q89, // +
        Q90, // +=
        Q91, // -
        Q92, // -=
        Q93, // =
        Q94, // ==
        Q95,
        Q96, // !=
        Q97, // <
        Q98, // <=
        Q99, // >
        Q100, // >=
        Q101, // ++
        Q102, // --
        Q103, // ,
        Q104, // :
        Q105, // ;
        Q106, // .
        Q107, // (
        Q108, // )
        Q109, // {
        Q110, // }
        Q111, // [
        Q112, // ] 
        Q113, // int val done
        Q114, // float val done
        Q115  // invalid done
    }

    // Function to execute state transitions
    private void automaton (char input) {
        switch (input) {
            case 'a':
                switch (currentState) {
                    case Q0:	currentState = State.Q1;
                    case Q8:	currentState = State.Q9;
                    case Q18:	currentState = State.Q19;
                    case Q25:	currentState = State.Q26;
                    case Q39:	currentState = State.Q40;
                    case Q52:	currentState = State.Q53;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'b':
                switch (currentState) {
                    case Q0:	currentState = State.Q4;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'c':
                switch (currentState) {
                    case Q0:	currentState = State.Q8;
                    case Q29:	currentState = State.Q30;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'd':
                switch (currentState) {
                    case Q2:	currentState = State.Q3;
                    case Q0:	currentState = State.Q12;
                    case Q46:	currentState = State.Q47;
                    case Q73:	currentState = State.Q74;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'e':
                switch (currentState) {
                    case Q0:	currentState = State.Q14;
                    case Q16:	currentState = State.Q17;
                    case Q21:	currentState = State.Q22;
                    case Q49:	currentState = State.Q50;
                    case Q51:	currentState = State.Q52;
                    case Q60:	currentState = State.Q61;
                    case Q64:	currentState = State.Q65;
                    case Q78:	currentState = State.Q79;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'f':
                switch (currentState) {
                    case Q0:	currentState = State.Q18;
                    case Q35:	currentState = State.Q36;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'g':
                switch (currentState) {
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'h':
                switch (currentState) {
                    case Q59:	currentState = State.Q60;
                    case Q75:	currentState = State.Q76;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'i':
                switch (currentState) {
                    case Q18:	currentState = State.Q23;
                    case Q31:	currentState = State.Q32;
                    case Q0:	currentState = State.Q35;
                    case Q40:	currentState = State.Q41;
                    case Q68:	currentState = State.Q69;
                    case Q72:	currentState = State.Q73;
                    case Q76:	currentState = State.Q77;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'j':
            case 'k':
                switch (currentState) {
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'l':
                switch (currentState) {
                    case Q6:	currentState = State.Q7;
                    case Q9:	currentState = State.Q10;
                    case Q10:	currentState = State.Q11;
                    case Q14:	currentState = State.Q15;
                    case Q18:	currentState = State.Q24;
                    case Q19:	currentState = State.Q20;
                    case Q69:	currentState = State.Q70;
                    case Q77:	currentState = State.Q78;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'm':
                switch (currentState) {
                    case Q0:	currentState = State.Q39;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'n':
                switch (currentState) {
                    case Q1:	currentState = State.Q2;
                    case Q28:	currentState = State.Q29;
                    case Q33:	currentState = State.Q34;
                    case Q35:	currentState = State.Q37;
                    case Q41:	currentState = State.Q42;
                    case Q0:	currentState = State.Q43;
                    case Q57:	currentState = State.Q58;
                    case Q61:	currentState = State.Q62;
                    case Q66:	currentState = State.Q67;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'o':
                switch (currentState) {
                    case Q4:	currentState = State.Q5;
                    case Q5:	currentState = State.Q6;
                    case Q12:	currentState = State.Q13;
                    case Q24:	currentState = State.Q25;
                    case Q32:	currentState = State.Q33;
                    case Q43:	currentState = State.Q44;
                    case Q0:	currentState = State.Q46;
                    case Q71:	currentState = State.Q72;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'p':
                switch (currentState) {
                    case Q50:	currentState = State.Q51;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'q':
                switch (currentState) {
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'r':
                switch (currentState) {
                    case Q46:	currentState = State.Q48;
                    case Q0:	currentState = State.Q49;
                    case Q56:	currentState = State.Q57;
                    case Q59:	currentState = State.Q63;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 's':
                switch (currentState) {
                    case Q20:	currentState = State.Q21;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 't':
                switch (currentState) {
                    case Q26:	currentState = State.Q27;
                    case Q30:	currentState = State.Q31;
                    case Q37:	currentState = State.Q38;
                    case Q44:	currentState = State.Q45;
                    case Q53:	currentState = State.Q54;
                    case Q50:	currentState = State.Q55;
                    case Q0:	currentState = State.Q59;
                    case Q67:	currentState = State.Q68;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'u':
                switch (currentState) {
                    case Q18:	currentState = State.Q28;
                    case Q55:	currentState = State.Q56;
                    case Q63:	currentState = State.Q64;
                    case Q0:	currentState = State.Q66;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'v':
                switch (currentState) {
                    case Q0:	currentState = State.Q71;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'w':
                switch (currentState) {
                    case Q0:	currentState = State.Q75;
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case 'x':
            case 'y':
            case 'z':
                switch (currentState) {
                    case Q115:	currentState = State.Q115;
                    default:	currentState = State.Q80;
                }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                switch (currentState) {
                    case Q0:	currentState = State.Q113;
                    case Q113:	currentState = State.Q113;
                    case Q91:	currentState = State.Q113;
                    case Q106:	currentState = State.Q114;
                    case Q114:	currentState = State.Q114;
                    default:	currentState = State.Q115;
                }
            case '^':
                switch (currentState) {
                    case Q0:	currentState = State.Q81;
                    default:	currentState = State.Q115;
                }
            case '*':
                switch (currentState) {
                    case Q0:	currentState = State.Q83;
                    default:	currentState = State.Q115;
                }
            case '/':
                switch (currentState) {
                    case Q0:	currentState = State.Q85;
                    default:	currentState = State.Q115;
                }
            case '%':
                switch (currentState) {
                    case Q0:	currentState = State.Q87;
                    default:	currentState = State.Q115;
                }
            case '+':
                switch (currentState) {
                    case Q0:	currentState = State.Q89;
                    case Q89:	currentState = State.Q101;
                    default:	currentState = State.Q115;
                }
            case '-':
                switch (currentState) {
                    case Q0:	currentState = State.Q91;
                    case Q91:	currentState = State.Q102;
                    default:	currentState = State.Q115;
                }
            case '=':
                switch (currentState) {
                    case Q0:	currentState = State.Q93;
                    case Q81:	currentState = State.Q82;
                    case Q83:	currentState = State.Q84;
                    case Q85:	currentState = State.Q86;
                    case Q87:	currentState = State.Q88;
                    case Q89:	currentState = State.Q90;
                    case Q91:	currentState = State.Q92;
                    case Q93:	currentState = State.Q94;
                    case Q95:	currentState = State.Q96;
                    case Q97:	currentState = State.Q98;
                    case Q99:	currentState = State.Q100;
                    default:	currentState = State.Q115;
                }
            case '!':
                switch (currentState) {
                    case Q0:	currentState = State.Q95;
                    default:	currentState = State.Q115;
                }
            case '<':
                switch (currentState) {
                    case Q0:	currentState = State.Q97;
                    default:	currentState = State.Q115;
                }
            case '>':
                switch (currentState) {
                    case Q0:	currentState = State.Q99;
                    default:	currentState = State.Q115;
                }
            case '(':
                switch (currentState) {
                    case Q0:	currentState = State.Q107;
                    default:	currentState = State.Q115;
                }
            case ')':
                switch (currentState) {
                    case Q0:	currentState = State.Q108;
                    default:	currentState = State.Q115;
                }
            case '{':
                switch (currentState) {
                    case Q0:	currentState = State.Q109;
                    default:	currentState = State.Q115;
                }
            case '}':
                switch (currentState) {
                    case Q0:	currentState = State.Q110;
                    default:	currentState = State.Q115;
                }
            case '[':
                switch (currentState) {
                    case Q0:	currentState = State.Q111;
                    default:	currentState = State.Q115;
                }
            case ']':
                switch (currentState) {
                    case Q0:	currentState = State.Q112;
                    default:	currentState = State.Q115;
                }
            case ',':
                switch (currentState) {
                    case Q0:	currentState = State.Q103;
                    default:	currentState = State.Q115;
                }
            case ':':
                switch (currentState) {
                    case Q0:	currentState = State.Q104;
                    default:	currentState = State.Q115;
                }
            case ';':
                switch (currentState) {
                    case Q0:	currentState = State.Q105;
                    default:	currentState = State.Q115;
                }
            case '.':
                switch (currentState) {
                    case Q113:	currentState = State.Q106;
                    default:	currentState = State.Q115;
                }
            default:
                currentState = State.Q115;
        }
    }

    @Override
    public String toString () {
        return "Line: " + lineNum + ", Char: " + charPos + ", Lexeme: " + lexeme;
    }
}
