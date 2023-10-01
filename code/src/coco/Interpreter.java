package coco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Interpreter {

    public class Pair {
        String type;
        Object value;
        Pair (String type, Object value) {
            this.type = type;
            this.value = value;
        }
        public boolean isType(String type) {
            return (this.type == type);
        }
        public Object get(String type) {
            if (isType(type))
                return value;
            return null;
        }
        // private HashMap<String, Object> vars = new HashMap<>();
    }

    // Error Reporting ============================================================
    private StringBuilder errorBuffer = new StringBuilder();

    private String reportSyntaxError (NonTerminal nt) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private String reportSyntaxError (Token.Kind kind) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    public String errorReport () {
        return errorBuffer.toString();
    }

    public boolean hasError () {
        return errorBuffer.length() != 0;
    }

    private class QuitParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public QuitParseException (String errorMessage) {
            super(errorMessage);
        }
    }

    private int lineNumber () {
        return currentToken.lineNumber();
    }

    private int charPosition () {
        return currentToken.charPosition();
    }

// Interpreter ============================================================
    private Scanner scanner;
    private Token currentToken;

    private BufferedReader reader;
    private StringTokenizer st;

    // TODO: add maps from Token IDENT to int/float/bool
    // private static HashMap<String, Object> vars = new HashMap<>();
    private static HashMap<String, Pair> vars = new HashMap<>();

    public Interpreter (Scanner scanner, InputStream in) {
        this.scanner = scanner;
        currentToken = this.scanner.next();

        reader = new BufferedReader(new InputStreamReader(in));
        st = null;
    }

    public void interpret () {
        try {
            computation();
        }
        catch (QuitParseException q) {
            // too verbose
            // errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            // errorBuffer.append("[Could not complete parsing.]");
        }
    }

// Helper Methods =============================================================
    private boolean have (Token.Kind kind) {
        return currentToken.is(kind);
    }

    private boolean have (NonTerminal nt) {
        return nt.firstSet().contains(currentToken.kind);
    }

    private boolean accept (Token.Kind kind) {
        if (have(kind)) {
            try {
                currentToken = scanner.next();
            }
            catch (NoSuchElementException e) {
                if (!kind.equals(Token.Kind.EOF)) {
                    String errorMessage = reportSyntaxError(kind);
                    throw new QuitParseException(errorMessage);
                }
            }
            return true;
        }
        return false;
    }

    private boolean accept (NonTerminal nt) {
        if (have(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }

    private boolean expect (Token.Kind kind) {
        if (accept(kind)) {
            return true;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private boolean expect (NonTerminal nt) {
        if (accept(nt)) {
            return true;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve (Token.Kind kind) {
        Token tok = currentToken;
        if (accept(kind)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve (NonTerminal nt) {
        Token tok = currentToken;
        if (accept(nt)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

// Pre-defined Functions ======================================================
    private String nextInput () {
        while (st == null || !st.hasMoreElements()) {
            try {
                st = new StringTokenizer(reader.readLine());
            }
            catch (IOException e) {
                throw new QuitParseException("Interepter: Couldn't read data file\n" + e.getMessage());
            }
        }
        return st.nextToken();
    }

    private int readInt () {
        System.out.print("int? ");
        return Integer.parseInt(nextInput());
    }

    private float readFloat () {
        System.out.print("float? ");
        return Float.parseFloat(nextInput());
    }

    private boolean readBool () {
        System.out.print("true or false? ");
        return Boolean.parseBoolean(nextInput());
    }

    private void printInt (int x) {
        System.out.print(x + " ");
    }

    private void printFloat (float x) {
        System.out.printf("%.2f ",x);
    }

    private void printBool (boolean x) {
        System.out.print(x + " ");
    }

    private void println () {
        System.out.println();
    }

// Grammar Rules ==============================================================

    // function for matching rule that only expects nonterminal's FIRST set
    private Token matchNonTerminal (NonTerminal nt) {
        return expectRetrieve(nt);
    }

    // TODO: implement operators and type grammar rules
    
    // powOp = "∧"
    private Token powOp () {
        return matchNonTerminal(NonTerminal.POW_OP);
    }
    // mulOp = "⋆" | "/" | "%" | "and"
    private Token mulOp () {
        return matchNonTerminal(NonTerminal.MUL_OP);
    }
    // addOp = "+" | "-" | "or"
    private Token addOp () {
        return matchNonTerminal(NonTerminal.ADD_OP);
    }
    // relOp = "==" | "!=" | "<" | "<=" | ">" | ">="
    private Token relOp () {
        return matchNonTerminal(NonTerminal.REL_OP);
    }
    // assignOp = "=" | "+=" | "-=" | "*=" | "/=" | "%=" | "∧="
    private Token assignOp () {
        return matchNonTerminal(NonTerminal.ASSIGN_OP);
    }
    // unaryOp = "++" | "--"
    private Token unaryOp () {
        return matchNonTerminal(NonTerminal.UNARY_OP);
    }
    // type = "bool" | "int" | "float"
    private Token type () {
        return matchNonTerminal(NonTerminal.TYPE);
    }
    // literal = boolLit
    private Token boolLit () {
        return matchNonTerminal(NonTerminal.BOOL_LIT);
    }
    // literal = integerLit | floatLit
    private Token literal () {
        return matchNonTerminal(NonTerminal.LITERAL);
    }

    // private void error(String message) {
    //     errorBuffer.append(message + "\n");
    //     throw new QuitParseException(message);
    // }

    // designator = ident { "[" relExpr "]" }
    private String designator () {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Token ident = expectRetrieve(Token.Kind.IDENT);

        // TODO: get designated value from appropriate map from IDENT to value
        return ident.lexeme();
        // Object val;
        // while (accept(Token.Kind.OPEN_BRACKET)) {
        //     relExpr();
        //     expect(Token.Kind.CLOSE_BRACKET);
        // }
        // if (vars.containsKey(ident.lexeme())) {
        //     return vars.get(ident.lexeme());
        // } else {
        //  // error("Variable " + ident.lexeme() + " at " + lineNum + "," + charPos + " not declared or initialized.");
        //     return null;
        // }
    }

    // TODO: implement remaining grammar rules
    // groupExpr = literal | designator | "not" relExpr | relation | funcCall
    private Object groupExpr () {
        if (have(NonTerminal.LITERAL)) {
            Token lit = literal();
            if (lit.kind == Token.Kind.INT_VAL)
                return Integer.parseInt(lit.lexeme());
            if (lit.kind == Token.Kind.FLOAT_VAL)
                return Float.parseFloat(lit.lexeme());
        } else if (have(NonTerminal.BOOL_LIT)) {
            return Boolean.parseBoolean(boolLit().lexeme());
        } else if (have(NonTerminal.DESIGNATOR)) {
            String varName = designator();
            String type = vars.get(varName).type;
            Object var = null;
            switch (type) {
                case "bool": var = vars.get(varName).get("bool");
                case "int": var = vars.get(varName).get("int");
                case "float": var = vars.get(varName).get("float");
            }
            // Object var = vars.get(varName);
            System.out.println("Fetching var " + varName + " with value " + var);
            return var;
        } else if (accept(Token.Kind.NOT)) {
            Object notRel = relExpr();
            if (notRel instanceof Boolean) {
                return !((boolean) notRel);
            } else {
                // error("Cannot negate value of " + notRel);
            }
        } else if (have(NonTerminal.RELATION)) {
            return relation();
        } else if (have(NonTerminal.FUNC_CALL)) {
            Object ret = funcCall();
            if (ret != null) return ret;
        }
        // error("Cannot deduce type of group expression.");
        return null;
    }

    // powExpr = groupExpr { powOp groupExpr }
    private Object powExpr () {
        Object left = groupExpr();
        if (left instanceof Integer) {
            int base = (int) left;
            while (accept(Token.Kind.POW)) {
                Object expObj = groupExpr();
                if (expObj instanceof Integer) {
                    base = (int) Math.pow((double) base, (double) ((int) expObj));
                } else {
                 // error("Exponent cannot be" + expObj + ": Not an int");
                }
            }
            return base;
        } else {
            if (have(NonTerminal.POW_OP)) {
             // error("Cannot exponentiate " + left + "; not an int");
            }
            return left;
        }
    }

    // mulExpr = powExpr { mulOp powExpr }
    private Object mulExpr () {
        Object left = powExpr();
        if (left instanceof Boolean) {
            boolean leftBool = (boolean) left;
            while (accept(Token.Kind.AND)) {
                Object right = powExpr();
                if (right instanceof Boolean) {
                    leftBool &= (boolean) right;
                } else {
                 // error("Cannot AND " + leftBool + " with " + right);
                }
            }
            return leftBool;
        } else if (left instanceof Integer) {
            int leftInt = (int) left;
            while (accept(NonTerminal.MUL_OP)) {
                Object right = powExpr();
                switch (currentToken.kind()) {
                    case MUL:
                        if (right instanceof Integer) {
                            leftInt *= (int) right;
                        } else {
                         // error("Cannot multiply int with " + right);
                        }
                        break;
                    case DIV:
                        if (right instanceof Integer) {
                            leftInt /= (int) right;
                        } else {
                         // error("Cannot divide int with " + right);
                        }
                        break;
                    case MOD:
                        if (right instanceof Integer) {
                            leftInt %= (int) right;
                        } else {
                         // error("Cannot mod int with " + right);
                        }
                        break;
                    default:
                     // error("Cannot AND ints");
                }
            }
            return leftInt;
        } else if (left instanceof Float) {
            float leftFloat = (float) left;
            while (accept(NonTerminal.MUL_OP)) {
                Object right = powExpr();
                switch (currentToken.kind()) {
                    case MUL:
                        if (right instanceof Integer) {
                            leftFloat *= (float) right;
                        } else {
                         // error("Cannot multiply float with " + right);
                        }
                        break;
                    case DIV:
                        if (right instanceof Integer) {
                            leftFloat /= (float) right;
                        } else {
                         // error("Cannot divide float with " + right);
                        }
                        break;
                    case MOD:
                        if (right instanceof Integer) {
                            leftFloat %= (float) right;
                        } else {
                         // error("Cannot mod float with " + right);
                        }
                        break;
                    default:
                     // error("Cannot AND floats");
                }
            }
            return leftFloat;
        } else {
         // error("Could not deduce type of " + left);
            return null;
        }
    }
    
    // addExpr = mulExpr { addOp mulExpr }
    private Object addExpr () {
        Object left = mulExpr();
        if (left instanceof Boolean) {
            boolean leftBool = (boolean) left;
            while (accept(Token.Kind.OR)) {
                Object right = mulExpr();
                if (right instanceof Boolean) {
                    leftBool |= (boolean) right;
                } else {
                 // error("Cannot OR " + leftBool + " with " + right);
                }
            }
            return leftBool;
        } else if (left instanceof Integer) {
            int leftInt = (int) left;
            while (accept(NonTerminal.ADD_OP)) {
                Object right = mulExpr();
                switch (currentToken.kind()) {
                    case ADD:
                        if (right instanceof Integer) {
                            leftInt += (int) right;
                        } else {
                         // error("Cannot add int with " + right);
                        }
                        break;
                    case SUB:
                        if (right instanceof Integer) {
                            leftInt -= (int) right;
                        } else {
                         // error("Cannot subtract int with " + right);
                        }
                        break;
                    default:
                     // error("Cannot OR ints");
                }
            }
            return leftInt;
        } else if (left instanceof Float) {
            float leftFloat = (float) left;
            while (accept(NonTerminal.ADD_OP)) {
                Object right = mulExpr();
                switch (currentToken.kind()) {
                    case MUL:
                        if (right instanceof Integer) {
                            leftFloat += (float) right;
                        } else {
                         // error("Cannot add float with " + right);
                        }
                        break;
                    case DIV:
                        if (right instanceof Integer) {
                            leftFloat -= (float) right;
                        } else {
                         // error("Cannot subtract float with " + right);
                        }
                        break;
                    default:
                     // error("Cannot OR floats");
                }
            }
            return leftFloat;
        } else {
         // error("Could not deduce type of " + left);
            return null;
        }
    }

    // relExpr = addExpr { relOp addExpr }
    private Object relExpr () {
        Object left = addExpr();
        Boolean ret = null;
        while (accept(NonTerminal.REL_OP)) {
            Token op = currentToken;
            Object right = mulExpr();
            switch (currentToken.kind()) {
                case EQUAL_TO:
                    if (left instanceof Integer && right instanceof Integer ||
                        left instanceof Float && right instanceof Float) {
                        return left == right;
                    } else if (left instanceof Boolean && right instanceof Boolean) {
                        if (ret == null) ret = left == right;
                        else ret = ret == right; 
                    }
                    break;
                case NOT_EQUAL:
                    if (left instanceof Integer && right instanceof Integer ||
                        left instanceof Float && right instanceof Float) {
                        return left != right;
                    } else if (left instanceof Boolean && right instanceof Boolean) {
                        if (ret == null) ret = left != right;
                        else ret = ret != right; 
                    }
                    break;
                case LESS_THAN:
                    if (left instanceof Integer && right instanceof Integer)
                        return (int) left < (int) right;
                    if (left instanceof Float && right instanceof Float)
                        return (float) left < (float) right;
                    break;
                case LESS_EQUAL:
                    if (left instanceof Integer && right instanceof Integer)
                        return (int) left <= (int) right;
                    if (left instanceof Float && right instanceof Float)
                        return (float) left <= (float) right;
                    break;
                case GREATER_THAN:
                    if (left instanceof Integer && right instanceof Integer)
                        return (int) left > (int) right;
                    if (left instanceof Float && right instanceof Float)
                        return (float) left > (float) right;
                    break;
                case GREATER_EQUAL:
                    if (left instanceof Integer && right instanceof Integer)
                        return (int) left >= (int) right;
                    if (left instanceof Float && right instanceof Float)
                        return (float) left >= (float) right;
                    break;
                default:
                 // error("Could not deduce operator " + op.lexeme());
            }
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    // relation = "(" relExpr ")"
    private Object relation () {
        expect(Token.Kind.OPEN_PAREN);
        Object ret = relExpr();
        expect(Token.Kind.CLOSE_PAREN);
        return ret;
    }

    // assign = designator ( ( assignOp relExpr ) | unaryOp )
    private void assign () {
        String key = designator();
        if (vars.containsKey(key)) {
            // Object val = vars.get(key);
            Pair val = vars.get(key);
            switch (val.type) {
                case "bool": System.out.println("Old value of var " + key + ": " + val.get("bool")); break;
                case "int": System.out.println("Old value of var " + key + ": " + val.get("int")); break;
                case "float": System.out.println("Old value of var " + key + ": " + val.get("float")); break;
            }
            // System.out.println("\nOld value of var " + key + ": " + val.get(key));
            if (have(NonTerminal.UNARY_OP)) {
                switch (unaryOp().kind()) {
                    case UNI_INC:
                        if (val.isType("int")) {
                        // if (val instanceof Integer) {
                            // vars.put(key, (int) val + 1);
                            vars.put(key, new Pair("int", (int) val.get("int") + 1));
                            return;
                        }
                        if (val.isType("float")) {
                        // if (val instanceof Float) {
                            // vars.put(key, (float) val + 1);
                            vars.put(key, new Pair("float", (float) val.get("float") + 1));
                            return;
                        }
                        break;
                    case UNI_DEC:
                        if (val.isType("int")) {
                        // if (val instanceof Integer) {
                            // vars.put(key, (int) val - 1);
                            vars.put(key, new Pair("int", (int) val.get("int") - 1));
                            return;
                        }
                        if (val.isType("float")) {
                        // if (val instanceof Float) {
                            // vars.put(key, (float) val - 1);
                            vars.put(key, new Pair("float", (float) val.get("float") - 1));
                            return;
                        }
                        break;
                    default:
                }
            } else if (have(NonTerminal.ASSIGN_OP)) {
                Token op = assignOp();
                Object relExpr = relExpr();
                if (relExpr instanceof Boolean) System.out.print("[bool] ");
                if (relExpr instanceof Integer) System.out.print("[int] ");
                if (relExpr instanceof Float) System.out.print("[float] ");
                System.out.println("relExpr: " + relExpr);
                switch (op.kind()) {
                    case ASSIGN:
                        if (val.isType("bool") && relExpr instanceof Boolean) {
                            System.out.println("Assigning bool to " + key);
                            vars.put(key, new Pair("bool", relExpr));
                            return;
                        }
                        if (val.isType("int") && relExpr instanceof Integer) {
                            System.out.println("Assigning int to " + key);
                            vars.put(key, new Pair("int", relExpr));
                            return;
                        }
                        if (val.isType("float") && relExpr instanceof Float) {
                            System.out.println("Assigning float to " + key);
                            vars.put(key, new Pair("float", relExpr));
                            return;
                        }
                        // if (val instanceof Boolean && relExpr instanceof Boolean || 
                        //     val instanceof Integer && relExpr instanceof Integer || 
                        //     val instanceof Float && relExpr instanceof Float ) {
                        //     vars.put(key, relExpr);
                        //     System.out.println("\nNew value of " + key + ": " + vars.get(key));
                        //     return;
                        // }
                        break;
                    case ADD_ASSIGN:
                        if (val.isType("int") && relExpr instanceof Integer) {
                            vars.put(key, new Pair("int", (int) val.get("int") + (int) relExpr));
                            return;
                        }
                        if (val.isType("float") && relExpr instanceof Float) {
                            vars.put(key, new Pair("float", (float) val.get("float") + (float) relExpr));
                            return;
                        }
                        // if (val instanceof Integer && relExpr instanceof Integer) {
                        //     vars.put(key, (int) val + (int) relExpr);
                        //     return;
                        // }
                        // if (val instanceof Float && relExpr instanceof Float) {
                        //     vars.put(key, (float) val + (float) relExpr);    
                        //     return;
                        // }
                        break;
                    case SUB_ASSIGN:
                        if (val.isType("int") && relExpr instanceof Integer) {
                            vars.put(key, new Pair("int", (int) val.get("int") - (int) relExpr));
                            return;
                        }
                        if (val.isType("float") && relExpr instanceof Float) {
                            vars.put(key, new Pair("float", (float) val.get("float") - (float) relExpr));
                            return;
                        }
                        // if (val instanceof Integer && relExpr instanceof Integer) {
                        //     vars.put(key, (int) val - (int) relExpr);
                        //     return;
                        // }
                        // if (val instanceof Float && relExpr instanceof Float) {
                        //     vars.put(key, (float) val - (float) relExpr);    
                        //     return;
                        // }
                        break;
                    case MUL_ASSIGN:
                        if (val.isType("int") && relExpr instanceof Integer) {
                            vars.put(key, new Pair("int", (int) val.get("int") * (int) relExpr));
                            return;
                        }
                        if (val.isType("float") && relExpr instanceof Float) {
                            vars.put(key, new Pair("float", (float) val.get("float") * (float) relExpr));
                            return;
                        }
                        // if (val instanceof Integer && relExpr instanceof Integer) {
                        //     vars.put(key, (int) val * (int) relExpr);
                        //     return;
                        // }
                        // if (val instanceof Float && relExpr instanceof Float) {
                        //     vars.put(key, (float) val * (float) relExpr);    
                        //     return;
                        // }
                        break;
                    case DIV_ASSIGN:
                        if (val.isType("int") && relExpr instanceof Integer) {
                            vars.put(key, new Pair("int", (int) val.get("int") / (int) relExpr));
                            return;
                        }
                        if (val.isType("float") && relExpr instanceof Float) {
                            vars.put(key, new Pair("float", (float) val.get("float") / (float) relExpr));
                            return;
                        }
                        // if (val instanceof Integer && relExpr instanceof Integer) {
                        //     vars.put(key, (int) val / (int) relExpr);
                        //     return;
                        // }
                        // if (val instanceof Float && relExpr instanceof Float) {
                        //     vars.put(key, (float) val / (float) relExpr);    
                        //     return;
                        // }
                        break;
                    case MOD_ASSIGN:
                        if (val.isType("int") && relExpr instanceof Integer) {
                            vars.put(key, new Pair("int", (int) val.get("int") % (int) relExpr));
                            return;
                        }
                        if (val.isType("float") && relExpr instanceof Float) {
                            vars.put(key, new Pair("float", (float) val.get("float") % (float) relExpr));
                            return;
                        }
                        // if (val instanceof Integer && relExpr instanceof Integer) {
                        //     vars.put(key, (int) val % (int) relExpr);
                        //     return;
                        // }
                        // if (val instanceof Float && relExpr instanceof Float) {
                        //     vars.put(key, (float) val % (float) relExpr);    
                        //     return;
                        // }
                        break;
                    case POW_ASSIGN:
                        if (val.isType("int") && relExpr instanceof Integer) {
                            vars.put(key, new Pair("int", Math.pow((int) val.get("int"), (int) relExpr)));
                            return;
                        }
                        // if (val instanceof Integer && relExpr instanceof Integer) {
                        //     vars.put(key, Math.pow((int) val, (int) relExpr));
                        //     return;
                        // }
                        break;
                    default:
                }
            }
         // error("No assignment operation provided.");
        } else {
         // error("Variable " + key + " not declared.");
        }
            
    }

    // funcCall = "call" ident "(" [ relExpr { "," relExpr } ] ")"
    private Object funcCall () {
        expect(Token.Kind.CALL);
        Token func = expectRetrieve(Token.Kind.IDENT);
        expect(Token.Kind.OPEN_PAREN);
        ArrayList<Object> args = new ArrayList<>();
        if (have(NonTerminal.REL_EXPR)) {
            do {
                args.add(relExpr());
            } while (accept(Token.Kind.COMMA));
        }
        // for (Object o : args) {
        //     System.out.println("DEBUG: " + o);
        // }
        expect(Token.Kind.CLOSE_PAREN);
        switch (func.lexeme()) {
            case "printInt":
                if (args.get(0) instanceof Integer) {
                    printInt((int) args.get(0));
                    return null;
                } else
                 // error("Cannot print " + args.get(0) + " with printInt");
                break;
            case "printFloat":
                if (args.get(0) instanceof Float) {
                    printFloat((float) args.get(0));
                    return null;
                } else
                 // error("Cannot print " + args.get(0) + " with printFloat");
                break;
            case "printBool":
                if (args.get(0) instanceof Boolean) {
                    printBool((boolean) args.get(0));
                    return null;
                } else
                 // error("Cannot print " + args.get(0) + " with printBool");
                break;
            case "println":
                if (args.size() == 0) {
                    println();
                    return null;
                } else
                 // error("println() arguments list not empty");
                break;
            case "readInt":
                if (args.size() == 0) {
                    return readInt();
                } else
                 // error("readInt() arguments list not empty");
                break;
            case "readFloat":
                if (args.size() == 0) {
                    return readFloat();
                } else
                 // error("readFloat() arguments list not empty");
                break;
            case "readBool":
                if (args.size() == 0) {
                    return readBool();
                } else
                 // error("readBool() arguments list not empty");
                break;
            default:
             // error("Undefined function call to " + func + "()");
        }
        return null;
    }

    // ifStat = "if" relation "then" statSeq [ "else" statSeq ] "fi"
    private void ifStat () {
        expect(Token.Kind.IF);
        boolean relation = (boolean) relation();
        expect(Token.Kind.THEN);
        if (relation) {
            statSeq();
            if (accept(Token.Kind.ELSE)) {
                while (!have(Token.Kind.FI)) {/* do nothing */}
            }
        } else {
            while (true) {
                if (have(Token.Kind.ELSE)) {
                    statSeq();
                    break;
                }
                if (have(Token.Kind.FI)) {
                    break;
                }
                scanner.next();
            }
        }
        expect(Token.Kind.FI);
    }

    // whileStat = "while" relation "do" statSeq "od"
    private void whileStat () {
        expect(Token.Kind.WHILE);
        relation();
        expect(Token.Kind.DO);
        while (!accept(Token.Kind.OD)) {}
    }

    // repeatStat = "repeat" statSeq "until" relation
    private void repeatStat () {
        expect(Token.Kind.REPEAT);
        while (!accept(Token.Kind.UNTIL)) {}
        relation();
    }

    // returnStat = "return" [ relExpr ]
    private Object returnStat () {
        expect(Token.Kind.RETURN);
        if (have(NonTerminal.REL_EXPR)) {
            return relExpr();
        }
        return null;
    }

    // statement = assign | funcCall | ifStat | whileStat | repeatStat | returnStat
    private Object statement () {
        if      (have(NonTerminal.ASSIGN))      assign();
        else if (have(NonTerminal.FUNC_CALL))   funcCall();
        else if (have(NonTerminal.IF_STAT))     ifStat();
        else if (have(NonTerminal.WHILE_STAT))  whileStat();
        else if (have(NonTerminal.REPEAT_STAT)) repeatStat();
        else {
            return returnStat();
        }
        return "not returnStat";
    }

    // statSeq = statement ";" { statement ";" }
    private void statSeq () {
        Object ret;
        do {
            ret = statement();
            expect(Token.Kind.SEMICOLON);
            if (ret instanceof String) {}
            else while (!have(Token.Kind.CLOSE_BRACE)) {
                scanner.next();
            }
        } while (have(NonTerminal.STATEMENT));
    }

    // typeDecl = type { "[" integerLit "]" }
    private String typeDecl () {
        String type = type().lexeme();
        while (accept(Token.Kind.OPEN_BRACKET)) {
            int dimSize = Integer.parseInt(literal().lexeme());
            expect(Token.Kind.CLOSE_BRACKET);
        }
        return type;
    }

    // varDecl = typeDecl ident { "," ident } ";"
    private void varDecl () {
        String typeClass = typeDecl();
        String name;
        do {
            name = expectRetrieve(Token.Kind.IDENT).lexeme();
            vars.put(name, new Pair(typeClass, null));
            // switch (typeClass) {
            //     case "bool":
            //         Boolean myBool = null;
            //         vars.put(name, new Pair("bool", myBool));
            //         // vars.put(name, myBool);
            //         break;
            //     case "int":
            //         Integer myInt = null;
            //         vars.put(name, new Pair("int", myInt));
            //         // vars.put(name, myInt);
            //         break;
            //     case "float":
            //         Float myFloat = null;
            //         vars.put(name, new Pair("float", myFloat));
            //         // vars.put(name, myFloat);
            //         break;
            // }
        } while (accept(Token.Kind.COMMA));
        expect(Token.Kind.SEMICOLON);
    }

    // paramType = type { "[" "]" }
    private String paramType () {
        String type = type().lexeme();
        while (accept(Token.Kind.OPEN_BRACKET)) {
            expect(Token.Kind.CLOSE_BRACKET);
        }
        return type;
    }

    // paramDecl = paramType ident
    private String[] paramDecl () {
        String typeClass = paramType();
        String name = expectRetrieve(Token.Kind.IDENT).lexeme();
        return new String[] {typeClass, name};
    }

    // formalParam = "(" [ paramDecl { "," paramDecl } ] ")"
    private HashMap<String, Object> formalParam () {
        HashMap<String, Object> args = new HashMap<>();
        expect(Token.Kind.OPEN_PAREN);
        if (have(NonTerminal.PARAM_DECL)) {
            do {
                String typeClass, name, pair[];
                pair = paramDecl();
                typeClass = pair[0];
                name = pair[1];
                // name = expectRetrieve(Token.Kind.IDENT).lexeme();
                switch (typeClass) {
                    case "bool":
                        Boolean myBool = null;
                        args.put(name, myBool);
                        break;
                    case "int":
                        Integer myInt = null;
                        args.put(name, myInt);
                        break;
                    case "float":
                        Float myFloat = null;
                        args.put(name, myFloat);
                        break;
                }
            } while (accept(Token.Kind.COMMA));
        }
        expect(Token.Kind.CLOSE_PAREN);
        return args;
    }

    // funcBody = "{" { varDecl } statSeq "}" ";"
    private void funcBody () {
        expect(Token.Kind.OPEN_BRACE);
        while (have(NonTerminal.VAR_DECL)) {
            varDecl();
        }
        statSeq();
        expect(Token.Kind.CLOSE_BRACE);
        expect(Token.Kind.SEMICOLON);
    }

    // funcDecl = "function" ident formalParam ":" ( "void" | type ) funcBody .
    private void funcDecl () {
        expect(Token.Kind.FUNC);
        expect(Token.Kind.IDENT);
        formalParam();
        expect(Token.Kind.COLON);
        if (!accept(Token.Kind.VOID)) type();
        funcBody();
    }
    
    // computation	= "main" {varDecl} {funcDecl} "{" statSeq "}" "."
    private void computation () {
        
        expect(Token.Kind.MAIN);

        // deal with varDecl
        while (have(NonTerminal.VAR_DECL)) {
            varDecl();
        }
        while (have(NonTerminal.FUNC_DECL)) {
            funcDecl();
        }

        expect(Token.Kind.OPEN_BRACE);
        statSeq();
        expect(Token.Kind.CLOSE_BRACE);
        expect(Token.Kind.PERIOD);       
    }
}
