package coco;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Token {
  
  public enum Kind {
    // Boolean operators
    AND("and"),
    OR("or"),
    NOT("not"),
    
    // Arithmetic operators
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    POW("^"),
    
    // Relational operators
    EQUAL_TO("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_EQUAL(">="),
    
    // Assignment operators
    ASSIGN("="),
    ADD_ASSIGN("+="),
    SUB_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/="),
    MOD_ASSIGN("%="),
    POW_ASSIGN("^="),

    // Unary increment/decrement
    UNI_INC("++"),
    UNI_DEC("--"),

    // Primitive types
    VOID("void"),
    BOOL("bool"),
    INT("int"),
    FLOAT("float"),

    // Boolean literals
    TRUE("true"),
    FALSE("false"),

    // Region delimiters
    OPEN_PAREN("("),
    CLOSE_PAREN(")"),
    OPEN_BRACE("{"),
    CLOSE_BRACE("}"),
    OPEN_BRACKET("["),
    CLOSE_BRACKET("]"),

    // Field/record delimiters
    COMMA(","),
    COLON(":"),
    SEMICOLON(";"),
    PERIOD("."),

    // Control flow statements
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

    // Keywords
    MAIN("main"),
    FUNC("function"),

    // Special cases
    INT_VAL(),
    FLOAT_VAL(),
    IDENT(),
    EOF(),
    ERROR();

    private String defaultLexeme;

    Kind() { defaultLexeme = ""; }

    Kind(String lexeme) { defaultLexeme = lexeme; }

    public boolean hasStaticLexeme() { return defaultLexeme != null; }

    // OPTIONAL: convenience function - boolean matches (String lexeme)
    //           to report whether a Token.Kind has the given lexeme may
    //           be useful
    public boolean hasLexeme(String lexeme) { return defaultLexeme.equals(lexeme); }
  }

  private int lineNum;
  private int charPos;
  Kind kind;
  private String lexeme = "";

  // Regex patterns for special cases
  private static final Pattern intPattern = Pattern.compile("^-?[0-9]+$");
  private static final Pattern floatPattern = Pattern.compile("^-?[0-9]+.[0-9]+$");
  private static final Pattern identPattern = Pattern.compile("^[a-z][_|[a-z]|[0-9]]*$", Pattern.CASE_INSENSITIVE);

  public static Token EOF(int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.kind = Kind.EOF;
    return tok;
  }

  public static Token ERROR(String lexeme, int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.kind = Kind.ERROR;
    tok.lexeme = lexeme;
    return tok;
  }

  public static Token INT(int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.kind = Kind.INT;
    return tok;
  }

  public static Token FLOAT(int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.kind = Kind.FLOAT;
    return tok;
  }

  public static Token BOOL(int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.kind = Kind.BOOL;
    return tok;
  }

  public static Token INT_VAL(String lexeme, int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.lexeme = lexeme;
    tok.kind = Kind.INT_VAL;
    return tok;
  }

  public static Token FLOAT_VAL(String lexeme, int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.lexeme = lexeme;
    tok.kind = Kind.FLOAT_VAL;
    return tok;
  }

  public static Token FALSE(int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.lexeme = "false";
    tok.kind = Kind.FALSE;
    return tok;
  }

  public static Token TRUE(int linePos, int charPos) {
    Token tok = new Token(linePos, charPos);
    tok.lexeme = "true";
    tok.kind = Kind.TRUE;
    return tok;
  }

  private Token(int lineNum, int charPos) {
    this.lineNum = lineNum;
    this.charPos = charPos;

    // No lexeme provided, signal error
    this.kind = Kind.ERROR;
    this.lexeme = "No Lexeme Given";
  }

  public Token(String lexeme, int lineNum, int charPos) {
    this.lineNum = lineNum;
    this.charPos = charPos;
    this.lexeme = lexeme;

    // Check if it matches a static value
    for (Kind k: Kind.values()) {
      if (k.hasStaticLexeme() && k.hasLexeme(lexeme)) {
        kind = k;
        return;
      }
    }

    // Check if it matches special cases (int, float, identifier, eof)
    Matcher matcher;
    matcher = intPattern.matcher(lexeme);
    if (matcher.matches()) {
      kind = Kind.INT_VAL;
      return;
    }
    matcher = floatPattern.matcher(lexeme);
    if (matcher.matches()) {
      kind = Kind.FLOAT_VAL;
      return;
    }
    matcher = identPattern.matcher(lexeme);
    if (matcher.matches()) {
      kind = Kind.IDENT;
      return;
    }

    // If we don't match anything, signal error
    this.kind = Kind.ERROR;
    this.lexeme = "Unrecognized lexeme: " + lexeme;
  }

  public int lineNumber() { return lineNum; }

  public int charPosition() { return charPos; }

  public String lexeme() { return lexeme; }

  public Kind kind() { return kind; }

  public boolean is(Kind kind) { return this.kind.equals(kind); }

  // OPTIONAL: add any additional helper or convenience methods
  //           that you find make for a cleaner design

  @Override
  public String toString() { return "Line: " + lineNum + ", Char: " + charPos + ", Lexeme: " + lexeme; }
}
