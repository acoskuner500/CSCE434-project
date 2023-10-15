package coco;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Scanner implements Iterator<Token> {

  private BufferedReader input;   // Buffered reader to read file
  private boolean closed;         // Flag for whether reader is closed or not

  private int lineNum;            // Current line number
  private int charPos;            // Character offset on current line

  private String scan;            // Current lexeme being scanned in
  private int nextChar;           // Contains the next char (-1 == EOF)

  // Reader will be a FileReader over the source file
  public Scanner(Reader reader) throws IOException {
    input = new BufferedReader(reader);
    closed = false;
    lineNum = 1;
    charPos = 0;
    scan = "";
    nextChar = input.read();
  }

  // Signal an error message
  public void Error(String msg, Exception e) {
    System.err.println("Scanner: Line - " + lineNum + ", Char - " + charPos);
    if (e != null) {
      e.printStackTrace();
    }
    System.err.println(msg);
  }

  /*
   * Helper function for reading a single char from input.
   * Can be used to catch and handle any IOExceptions,
   * advance the charPos or lineNum, etc.
   */
  private int readChar() {
    int curr = nextChar;

    if (curr == '\n') {
      lineNum++;
      charPos = 0;
    } else {
      charPos++;
    }
    
    try {
      nextChar = input.read();
    } catch (IOException e) {
      nextChar = -1;
      return -1;
    }

    return curr;
  }

  /*
   * Function to query whether or not more characters can be read.
   * Depends on closed and nextChar.
   */
  @Override
  public boolean hasNext() {
    return !closed || (nextChar != -1);
  }

  /*
   * Returns next Token from input
   * 
   * Invariants:
   *    1. Call assumes that nextChar is already holding an unread character
   *    2. Return leaves nextChar containing an untokenized character
   *    3. Closes reader when emitting EOF
   */
  @Override
  public Token next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    int inChar, curChar, curLine;
    scan = "";

    // Check if end of file has been reached
    if (nextChar == -1) {
      closed = true;
      return Token.EOF(lineNum, charPos);
    }

    // Start getting lexemes
    while (nextChar != -1) {
      inChar = readChar();
      curChar = charPos;
      curLine = lineNum;

      // Skip whitespace
      if (inChar == ' ' || inChar == '\n' || inChar == '\t' || inChar == '\r') {
        continue;
      }

      // Check for identifier or keywords
      if (Character.isAlphabetic(inChar)) {
        scan += (char) inChar;
        return getIdentifierOrKeyword(curLine, curChar);
      }

      // Check if could be int or float
      if (Character.isDigit(inChar)) {
        // Start building until rule violated
        scan += (char) inChar;
        return getNumber(curLine, curChar);
      }

      // Could be comparison, arithmetic, or assignment
      if (Arrays.asList('=', '!', '<', '>', '+', '-', '*', '/', '%', '^').contains((char) inChar)) {
        // If second character is '=', then return as valid operator
        if (nextChar == '=') {
          scan += (char) inChar;
          scan += (char) readChar();
          return new Token(scan, curLine, curChar);
        }

        // Could be comment
        if (inChar == '/') {
          // Single-line comment
          if (nextChar == '/') {
            // Skip until end of line or file
            inChar = readChar();
            while (inChar != -1 && inChar != '\n') {
              inChar = readChar();
            }
            continue;
          }

          // Block comment
          if (nextChar == '*') {
            inChar = readChar();
            inChar = readChar();
            // Skip until closing "*/" or end of file
            while (nextChar != -1) {
              if (inChar == '*' && nextChar == '/') {
                readChar();
                break;
              }
              inChar = readChar();
            }
            if (nextChar == -1) {
              return Token.ERROR("Missing closing */", curLine, curChar);
            }
            // Continue looking for tokens
            continue;
          }

          // Must be div
        }

        // Could be unary operator
        if ((inChar == '+' || inChar == '-') && inChar == nextChar) {
          scan += (char) inChar;
          scan += (char) readChar();
          return new Token(scan, curLine, curChar);
        }

        // Determine if negative number or sub
        if (inChar == '-') {
          scan += (char) inChar;
          if (Character.isDigit(nextChar)) {
            scan += (char) nextChar;
            readChar();
            return getNumber(curLine, curChar);
          }
          return new Token(scan, curLine, curChar);
        }

        scan += (char) inChar;
        return new Token(scan, curLine, curChar);
      }

      // Check if single-character token
      if (Arrays.asList('(', ')', '{', '}', '[', ']', ':', ';', ',', '.').contains((char) inChar)) {
        scan += (char) inChar;
        return new Token(scan, curLine, curChar);
      }

      // Otherwise, bad character poisions consecutive characters
      scan += (char) inChar;
      return purgeConsecutiveCharacters(curLine, curChar);
    }

    closed = true;
    return Token.EOF(lineNum, charPos);
  }


  // OPTIONAL: add any additional helper or convenience methods
  //           that you find make for a cleaner design
  //           (useful for handling special case Tokens)
  private boolean nextCharacterValid() {
    Token temp = new Token("" + (char) nextChar, lineNum, charPos);
    if (temp.kind() != Token.Kind.ERROR || Arrays.asList('!', ' ', '\n', '\t', '\r').contains((char) nextChar)) {
      // Them we are good to return the token as is
      return true;
    }
    return false;
  }

  private Token purgeConsecutiveCharacters(int curLine, int curChar) {
    while (nextChar != ' ' && nextChar != '\n' && nextChar != '\t' && nextChar != -1 && nextChar != '\r') {
      scan += (char) readChar();
    }
    return new Token(scan, curLine, curChar);
  }

  private Token getIdentifierOrKeyword(int curLine, int curChar) {
    // Start building lexeme until identifier rule is violated
    while (Character.isLetterOrDigit(nextChar) || nextChar == '_') {
      scan += (char) readChar();
    }
    // Check if next symbol is not start of valid token (or whitespace)
    if (nextCharacterValid()) {
      // Then we are good to return the token as is
      return new Token(scan, curLine, curChar);
    }
    // Otherwise, stray token poisons all consecutive characters
    return purgeConsecutiveCharacters(curLine, curChar);
  }

  private Token getNumber(int curLine, int curChar) {
    while (Character.isDigit(nextChar)) {
      scan += (char) readChar();
    }

    // Could be float
    if (nextChar == '.') {
      scan += (char) nextChar;
      readChar();
      while (Character.isDigit(nextChar)) {
        scan += (char) readChar();
      }
    }
    // Check if next symbol is invalid, or no number following decimal
    if (!nextCharacterValid() || (scan.charAt(scan.length() - 1) == '.')) {
      return purgeConsecutiveCharacters(curLine, curChar);
    }
    // Otherwise we are good to return the token as is
    return new Token(scan, curLine, curChar);
  }
}