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
        this.kind = kindOfState(currentState);
        if (!isInvalidState(currentState)) {
            return;
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
    private static boolean isInvalidState(State state) {
        switch (state) {
            case Q0:
            case Q95:
            case Q114:
            case Q116:  return true;
            default:    return false;
        }
    }
    
    private static boolean isKeywordState(State state) {
        switch (state) {
            case Q3:
            case Q7:
            case Q11:
            case Q13:
            case Q17:
            case Q22:
            case Q23:
            case Q27:
            case Q34:
            case Q36:
            case Q38:
            case Q42:
            case Q45:
            case Q47:
            case Q48:
            case Q54:
            case Q58:
            case Q62:
            case Q65:
            case Q70:
            case Q74:
            case Q79:   return true;
            default:    return false;
        }
    }

    // states that have a final token but can transition
    // to another token, e.g. ^ to ^=, but not ! to !=
    // also includes intval and floatval
    public static boolean isTransientState(State state) {
        switch (state) {
            case Q81:
            case Q83:
            case Q85:
            case Q87:
            case Q89:
            case Q91:
            case Q93:
            case Q97:
            case Q99:
            case Q113:
            case Q115:  return true;
            default:    return false;
        }
    }

    public static boolean isFinalState(State state) {
        switch (state) {
            case Q82:
            case Q84:
            case Q86:
            case Q88:
            case Q90:
            case Q92:
            case Q94:
            case Q96:
            case Q98:
            case Q100:
            case Q101:
            case Q102:
            case Q103:
            case Q104:
            case Q105:
            case Q106:
            case Q107:
            case Q108:
            case Q109:
            case Q110:
            case Q111:
            case Q112:  return true;
            default:    return false;
        }
    }

    private static boolean isIdentTransientState(State state) {
        return !(isKeywordState(state) ||
                isTransientState(state) ||
                isFinalState(state) ||
                isInvalidState(state));
    }

    // Function to return Kind based on accepting states
    private Kind kindOfState(State state) {
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
            case Q113:  return Kind.INT_VAL;
            case Q115:  return Kind.FLOAT_VAL;
            case Q0:
            case Q95:   //no token for !
            case Q114:
            case Q116:  return Kind.ERROR;
            default:    return Kind.IDENT;
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
        Q95, // ! not token
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
        Q113, // int val
        Q114, // . not token
        Q115, // float val
        Q116  // invalid
    }
    // Function to execute state transitions
    public static State automaton(State state, char input) {
        switch (state) {
            case Q0:
                if (Character.isLetter(input)) {
                    switch (input) {
                        case 'a': return State.Q1;
                        case 'b': return State.Q4;
                        case 'c': return State.Q8;
                        case 'd': return State.Q12;
                        case 'e': return State.Q14;
                        case 'f': return State.Q18;
                        case 'i': return State.Q35;
                        case 'm': return State.Q39;
                        case 'n': return State.Q43;
                        case 'o': return State.Q46;
                        case 'r': return State.Q49;
                        case 't': return State.Q59;
                        case 'u': return State.Q66;
                        case 'v': return State.Q71;
                        case 'w': return State.Q75;
                        default: return State.Q80;
                    }
                } else if (Character.isDigit(input)) {
                    return State.Q113;
                } else {
                    switch (input) {
                        case '^': return State.Q81;
                        case '*': return State.Q83;
                        case '/': return State.Q85;
                        case '%': return State.Q87;
                        case '+': return State.Q89;
                        case '-': return State.Q91;
                        case '=': return State.Q93;
                        case '!': return State.Q95;
                        case '<': return State.Q97;
                        case '>': return State.Q99;
                        case ',': return State.Q103;
                        case ':': return State.Q104;
                        case ';': return State.Q105;
                        case '.': return State.Q106;
                        case '(': return State.Q107;
                        case ')': return State.Q108;
                        case '{': return State.Q109;
                        case '}': return State.Q110;
                        case '[': return State.Q111;
                        case ']': return State.Q112;
                        default: return State.Q116;
                    }
                }
            case Q1:
                if (isIdentChar(input)) {
                    if (input == 'n') return State.Q2;
                    else return State.Q80;
                } else return State.Q116;
            case Q2:
                if (isIdentChar(input)) {
                    if (input == 'd') return State.Q3;
                    else return State.Q80;
                } else return State.Q116;
            case Q3:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q4:
                if (isIdentChar(input)) {
                    if (input == 'o') return State.Q5;
                    else return State.Q80;
                } else return State.Q116;
            case Q5:
                if (isIdentChar(input)) {
                    if (input == 'o') return State.Q6;
                    else return State.Q80;
                } else return State.Q116;
            case Q6:
                if (isIdentChar(input)) {
                    if (input == 'l') return State.Q7;
                    else return State.Q80;
                } else return State.Q116;
            case Q7:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q8:
                if (isIdentChar(input)) {
                    if (input == 'a') return State.Q9;
                    else return State.Q80;
                } else return State.Q116;
            case Q9:
                if (isIdentChar(input)) {
                    if (input == 'l') return State.Q10;
                    else return State.Q80;
                } else return State.Q116;
            case Q10:
                if (isIdentChar(input)) {
                    if (input == 'l') return State.Q11;
                    else return State.Q80;
                } else return State.Q116;
            case Q11:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q12:
                if (isIdentChar(input)) {
                    if (input == 'o') return State.Q13;
                    else return State.Q80;
                } else return State.Q116;
            case Q13:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q14:
                if (isIdentChar(input)) {
                    if (input == 'l') return State.Q15;
                    else return State.Q80;
                } else return State.Q116;
            case Q15:
                if (isIdentChar(input)) {
                    if (input == 's') return State.Q16;
                    else return State.Q80;
                } else return State.Q116;
            case Q16:
                if (isIdentChar(input)) {
                    if (input == 'e') return State.Q17;
                    else return State.Q80;
                } else return State.Q116;
            case Q17:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q18:
                if (isIdentChar(input)) {
                    if (input == 'a') return State.Q19;
                    else if (input == 'i') return State.Q23;
                    else if (input == 'l') return State.Q24;
                    else if (input == 'u') return State.Q28;
                    else return State.Q80;
                } else return State.Q116;
            case Q19:
                if (isIdentChar(input)) {
                    if (input == 'l') return State.Q20;
                    else return State.Q80;
                } else return State.Q116;
            case Q20:
                if (isIdentChar(input)) {
                    if (input == 's') return State.Q21;
                    else return State.Q80;
                } else return State.Q116;
            case Q21:
                if (isIdentChar(input)) {
                    if (input == 'e') return State.Q22;
                    else return State.Q80;
                } else return State.Q116;
            case Q22:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q23:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q24:
                if (isIdentChar(input)) {
                    if (input == 'o') return State.Q25;
                    else return State.Q80;
                } else return State.Q116;
            case Q25:
                if (isIdentChar(input)) {
                    if (input == 'a') return State.Q26;
                    else return State.Q80;
                } else return State.Q116;
            case Q26:
                if (isIdentChar(input)) {
                    if (input == 't') return State.Q27;
                    else return State.Q80;
                } else return State.Q116;
            case Q27:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q28:
                if (isIdentChar(input)) {
                    if (input == 'n') return State.Q29;
                    else return State.Q80;
                } else return State.Q116;
            case Q29:
                if (isIdentChar(input)) {
                    if (input == 'c') return State.Q30;
                    else return State.Q80;
                } else return State.Q116;
            case Q30:
                if (isIdentChar(input)) {
                    if (input == 't') return State.Q31;
                    else return State.Q80;
                } else return State.Q116;
            case Q31:
                if (isIdentChar(input)) {
                    if (input == 'i') return State.Q32;
                    else return State.Q80;
                } else return State.Q116;
            case Q32:
                if (isIdentChar(input)) {
                    if (input == 'o') return State.Q33;
                    else return State.Q80;
                } else return State.Q116;
            case Q33:
                if (isIdentChar(input)) {
                    if (input == 'n') return State.Q34;
                    else return State.Q80;
                } else return State.Q116;
            case Q34:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q35:
                if (isIdentChar(input)) {
                    if (input == 'f') return State.Q36;
                    else if (input == 'n') return State.Q37;
                    else return State.Q80;
                } else return State.Q116;
            case Q36:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q37:
                if (isIdentChar(input)) {
                    if (input == 't') return State.Q38;
                    else return State.Q80;
                } else return State.Q116;
            case Q38:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q39:
                if (isIdentChar(input)) {
                    if (input == 'a') return State.Q40;
                    else return State.Q80;
                } else return State.Q116;
            case Q40:
                if (isIdentChar(input)) {
                    if (input == 'i') return State.Q41;
                    else return State.Q80;
                } else return State.Q116;
            case Q41:
                if (isIdentChar(input)) {
                    if (input == 'n') return State.Q42;
                    else return State.Q80;
                } else return State.Q116;
            case Q42:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q43:
                if (isIdentChar(input)) {
                    if (input == 'o') return State.Q44;
                    else return State.Q80;
                } else return State.Q116;
            case Q44:
                if (isIdentChar(input)) {
                    if (input == 't') return State.Q45;
                    else return State.Q80;
                } else return State.Q116;
            case Q45:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q46:
                if (isIdentChar(input)) {
                    if (input == 'd') return State.Q47;
                    else if (input == 'r') return State.Q48;
                    else return State.Q80;
                } else return State.Q116;
            case Q47:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q48:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q49:
                if (isIdentChar(input)) {
                    if (input == 'e') return State.Q50;
                    else return State.Q80;
                } else return State.Q116;
            case Q50:
                if (isIdentChar(input)) {
                    if (input == 'p') return State.Q51;
                    else if (input == 't') return State.Q55;
                    else return State.Q80;
                } else return State.Q116;
            case Q51:
                if (isIdentChar(input)) {
                    if (input == 'e') return State.Q52;
                    else return State.Q80;
                } else return State.Q116;
            case Q52:
                if (isIdentChar(input)) {
                    if (input == 'a') return State.Q53;
                    else return State.Q80;
                } else return State.Q116;
            case Q53:
                if (isIdentChar(input)) {
                    if (input == 't') return State.Q54;
                    else return State.Q80;
                } else return State.Q116;
            case Q54:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q55:
                if (isIdentChar(input)) {
                    if (input == 'u') return State.Q56;
                    else return State.Q80;
                } else return State.Q116;
            case Q56:
                if (isIdentChar(input)) {
                    if (input == 'r') return State.Q57;
                    else return State.Q80;
                } else return State.Q116;
            case Q57:
                if (isIdentChar(input)) {
                    if (input == 'n') return State.Q58;
                    else return State.Q80;
                } else return State.Q116;
            case Q58:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q59:
                if (isIdentChar(input)) {
                    if (input == 'h') return State.Q60;
                    if (input == 'r') return State.Q63;
                    else return State.Q80;
                } else return State.Q116;
            case Q60:
                if (isIdentChar(input)) {
                    if (input == 'e') return State.Q61;
                    else return State.Q80;
                } else return State.Q116;
            case Q61:
                if (isIdentChar(input)) {
                    if (input == 'n') return State.Q62;
                    else return State.Q80;
                } else return State.Q116;
            case Q62:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q63:
                if (isIdentChar(input)) {
                    if (input == 'u') return State.Q64;
                    else return State.Q80;
                } else return State.Q116;
            case Q64:
                if (isIdentChar(input)) {
                    if (input == 'e') return State.Q65;
                    else return State.Q80;
                } else return State.Q116;
            case Q65:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q66:
                if (isIdentChar(input)) {
                    if (input == 'n') return State.Q67;
                    else return State.Q80;
                } else return State.Q116;
            case Q67:
                if (isIdentChar(input)) {
                    if (input == 't') return State.Q68;
                    else return State.Q80;
                } else return State.Q116;
            case Q68:
                if (isIdentChar(input)) {
                    if (input == 'i') return State.Q69;
                    else return State.Q80;
                } else return State.Q116;
            case Q69:
                if (isIdentChar(input)) {
                    if (input == 'l') return State.Q70;
                    else return State.Q80;
                } else return State.Q116;
            case Q70:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q71:
                if (isIdentChar(input)) {
                    if (input == 'o') return State.Q72;
                    else return State.Q80;
                } else return State.Q116;
            case Q72:
                if (isIdentChar(input)) {
                    if (input == 'i') return State.Q73;
                    else return State.Q80;
                } else return State.Q116;
            case Q73:
                if (isIdentChar(input)) {
                    if (input == 'd') return State.Q74;
                    else return State.Q80;
                } else return State.Q116;
            case Q74:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q75:
                if (isIdentChar(input)) {
                    if (input == 'h') return State.Q76;
                    else return State.Q80;
                } else return State.Q116;
            case Q76:
                if (isIdentChar(input)) {
                    if (input == 'i') return State.Q77;
                    else return State.Q80;
                } else return State.Q116;
            case Q77:
                if (isIdentChar(input)) {
                    if (input == 'l') return State.Q78;
                    else return State.Q80;
                } else return State.Q116;
            case Q78:
                if (isIdentChar(input)) {
                    if (input == 'e') return State.Q79;
                    else return State.Q80;
                } else return State.Q116;
            case Q79:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q80:
                if (isIdentChar(input)) {
                    return State.Q80;
                } else return State.Q116;
            case Q81:
                if (input == '=') return State.Q82;
                else return State.Q116;
            // case Q82:
            case Q83:
                if (input == '=') return State.Q84;
                else return State.Q116;
            // case Q84:
            case Q85:
                if (input == '=') return State.Q86;
                else return State.Q116;
            // case Q86:
            case Q87:
                if (input == '=') return State.Q88;
                else return State.Q116;
            // case Q88:
            case Q89:
                if (input == '=') return State.Q90;
                else if (input == '+') return State.Q101;
                else return State.Q116;
            // case Q90:
            case Q91:
                if (input == '=') return State.Q92;
                else if (input == '-') return State.Q102;
                else if (Character.isDigit(input)) return State.Q113;
                else return State.Q116;
            // case Q92:
            case Q93:
                if (input == '=') return State.Q94;
                else return State.Q116;
            // case Q94:
            case Q95:
                if (input == '=') return State.Q96;
                else return State.Q116;
            // case Q96:
            case Q97:
                if (input == '=') return State.Q98;
                else return State.Q116;
            // case Q98:
            case Q99:
                if (input == '=') return State.Q100;
                else return State.Q116;
            // case Q100:
            // case Q101:
            // case Q102:
            // case Q103:
            // case Q104:
            // case Q105:
            // case Q106:
            // case Q107:
            // case Q108:
            // case Q109:
            // case Q110:
            // case Q111:
            // case Q112:
            case Q113:
                if (input == '.') return State.Q114;
                else if (Character.isDigit(input)) return State.Q113;
                else return State.Q116;
            case Q114:
                if (Character.isDigit(input)) return State.Q115;
                else return State.Q116;
            case Q115:
                if (Character.isDigit(input)) return State.Q115;
                else return State.Q116;
            // case Q116:
            default: return State.Q116;
        }
    }

    private static boolean isIdentChar(char c) {
        return (Character.isLetterOrDigit(c) || (c == '_'));
    }

    @Override
    public String toString () {
        return "Line: " + lineNum + ", Char: " + charPos + ", Lexeme: " + lexeme;
    }
}
