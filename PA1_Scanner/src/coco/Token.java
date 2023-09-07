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
            currentState = automaton(currentState, c);
        }
        this.kind = acceptingState(currentState);
        if (!invalidState(currentState)) {
            return;
        }

        // if we don't match anything, signal error
        this.kind = Kind.ERROR;
        this.lexeme = "Unrecognized lexeme: " + lexeme;
        System.out.print(this.lexeme + "\t");
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
    public static boolean invalidState(State state) {
        return (state == State.Q115);
    }

    public static boolean isAcceptingState (State state) {
        return (acceptingState(state) != Kind.ERROR);
    }

    // Function to return Kind based on accepting states
    private static Kind acceptingState(State state) {
        switch (state) {
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
    public enum State {
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
    public static State automaton (State state, char input) {
        switch (input) {
            case 'a':
                switch (state) {
                    case Q0:	return State.Q1;
                    case Q8:	return State.Q9;
                    case Q18:	return State.Q19;
                    case Q25:	return State.Q26;
                    case Q39:	return State.Q40;
                    case Q52:	return State.Q53;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'b':
                switch (state) {
                    case Q0:	return State.Q4;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'c':
                switch (state) {
                    case Q0:	return State.Q8;
                    case Q29:	return State.Q30;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'd':
                switch (state) {
                    case Q2:	return State.Q3;
                    case Q0:	return State.Q12;
                    case Q46:	return State.Q47;
                    case Q73:	return State.Q74;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'e':
                switch (state) {
                    case Q0:	return State.Q14;
                    case Q16:	return State.Q17;
                    case Q21:	return State.Q22;
                    case Q49:	return State.Q50;
                    case Q51:	return State.Q52;
                    case Q60:	return State.Q61;
                    case Q64:	return State.Q65;
                    case Q78:	return State.Q79;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'f':
                switch (state) {
                    case Q0:	return State.Q18;
                    case Q35:	return State.Q36;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'g':
                switch (state) {
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'h':
                switch (state) {
                    case Q59:	return State.Q60;
                    case Q75:	return State.Q76;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'i':
                switch (state) {
                    case Q18:	return State.Q23;
                    case Q31:	return State.Q32;
                    case Q0:	return State.Q35;
                    case Q40:	return State.Q41;
                    case Q68:	return State.Q69;
                    case Q72:	return State.Q73;
                    case Q76:	return State.Q77;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'j':
            case 'k':
                switch (state) {
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'l':
                switch (state) {
                    case Q6:	return State.Q7;
                    case Q9:	return State.Q10;
                    case Q10:	return State.Q11;
                    case Q14:	return State.Q15;
                    case Q18:	return State.Q24;
                    case Q19:	return State.Q20;
                    case Q69:	return State.Q70;
                    case Q77:	return State.Q78;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'm':
                switch (state) {
                    case Q0:	return State.Q39;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'n':
                switch (state) {
                    case Q1:	return State.Q2;
                    case Q28:	return State.Q29;
                    case Q33:	return State.Q34;
                    case Q35:	return State.Q37;
                    case Q41:	return State.Q42;
                    case Q0:	return State.Q43;
                    case Q57:	return State.Q58;
                    case Q61:	return State.Q62;
                    case Q66:	return State.Q67;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'o':
                switch (state) {
                    case Q4:	return State.Q5;
                    case Q5:	return State.Q6;
                    case Q12:	return State.Q13;
                    case Q24:	return State.Q25;
                    case Q32:	return State.Q33;
                    case Q43:	return State.Q44;
                    case Q0:	return State.Q46;
                    case Q71:	return State.Q72;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'p':
                switch (state) {
                    case Q50:	return State.Q51;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'q':
                switch (state) {
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'r':
                switch (state) {
                    case Q46:	return State.Q48;
                    case Q0:	return State.Q49;
                    case Q56:	return State.Q57;
                    case Q59:	return State.Q63;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 's':
                switch (state) {
                    case Q20:	return State.Q21;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 't':
                switch (state) {
                    case Q26:	return State.Q27;
                    case Q30:	return State.Q31;
                    case Q37:	return State.Q38;
                    case Q44:	return State.Q45;
                    case Q53:	return State.Q54;
                    case Q50:	return State.Q55;
                    case Q0:	return State.Q59;
                    case Q67:	return State.Q68;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'u':
                switch (state) {
                    case Q18:	return State.Q28;
                    case Q55:	return State.Q56;
                    case Q63:	return State.Q64;
                    case Q0:	return State.Q66;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'v':
                switch (state) {
                    case Q0:	return State.Q71;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'w':
                switch (state) {
                    case Q0:	return State.Q75;
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
                }
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                switch (state) {
                    case Q80:	return State.Q80;
                    default:	return State.Q115;
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
                switch (state) {
                    case Q0:	return State.Q113;
                    case Q113:	return State.Q113;
                    case Q91:	return State.Q113;
                    case Q106:	return State.Q114;
                    case Q114:	return State.Q114;
                    default:	return State.Q115;
                }
            case '^':
                switch (state) {
                    case Q0:	return State.Q81;
                    default:	return State.Q115;
                }
            case '*':
                switch (state) {
                    case Q0:	return State.Q83;
                    default:	return State.Q115;
                }
            case '/':
                switch (state) {
                    case Q0:	return State.Q85;
                    default:	return State.Q115;
                }
            case '%':
                switch (state) {
                    case Q0:	return State.Q87;
                    default:	return State.Q115;
                }
            case '+':
                switch (state) {
                    case Q0:	return State.Q89;
                    case Q89:	return State.Q101;
                    default:	return State.Q115;
                }
            case '-':
                switch (state) {
                    case Q0:	return State.Q91;
                    case Q91:	return State.Q102;
                    default:	return State.Q115;
                }
            case '=':
                switch (state) {
                    case Q0:	return State.Q93;
                    case Q81:	return State.Q82;
                    case Q83:	return State.Q84;
                    case Q85:	return State.Q86;
                    case Q87:	return State.Q88;
                    case Q89:	return State.Q90;
                    case Q91:	return State.Q92;
                    case Q93:	return State.Q94;
                    case Q95:	return State.Q96;
                    case Q97:	return State.Q98;
                    case Q99:	return State.Q100;
                    default:	return State.Q115;
                }
            case '!':
                switch (state) {
                    case Q0:	return State.Q95;
                    default:	return State.Q115;
                }
            case '<':
                switch (state) {
                    case Q0:	return State.Q97;
                    default:	return State.Q115;
                }
            case '>':
                switch (state) {
                    case Q0:	return State.Q99;
                    default:	return State.Q115;
                }
            case '(':
                switch (state) {
                    case Q0:	return State.Q107;
                    default:	return State.Q115;
                }
            case ')':
                switch (state) {
                    case Q0:	return State.Q108;
                    default:	return State.Q115;
                }
            case '{':
                switch (state) {
                    case Q0:	return State.Q109;
                    default:	return State.Q115;
                }
            case '}':
                switch (state) {
                    case Q0:	return State.Q110;
                    default:	return State.Q115;
                }
            case '[':
                switch (state) {
                    case Q0:	return State.Q111;
                    default:	return State.Q115;
                }
            case ']':
                switch (state) {
                    case Q0:	return State.Q112;
                    default:	return State.Q115;
                }
            case ',':
                switch (state) {
                    case Q0:	return State.Q103;
                    default:	return State.Q115;
                }
            case ':':
                switch (state) {
                    case Q0:	return State.Q104;
                    default:	return State.Q115;
                }
            case ';':
                switch (state) {
                    case Q0:	return State.Q105;
                    default:	return State.Q115;
                }
            case '.':
                switch (state) {
                    case Q113:	return State.Q106;
                    default:	return State.Q115;
                }
            default:
                return State.Q115;
        }
    }

    @Override
    public String toString () {
        return "Line: " + lineNum + ", Char: " + charPos + ", Lexeme: " + lexeme;
    }
}
