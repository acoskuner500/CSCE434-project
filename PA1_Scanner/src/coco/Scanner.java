package coco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Scanner implements Iterator<Token> {

    private BufferedReader input;   // buffered reader to read file
    private boolean closed; // flag for whether reader is closed or not

    private int lineNum;    // current line number
    private int charPos;    // character offset on current line

    private String scan;    // current lexeme being scanned in
    private int nextChar;   // contains the next char (-1 == EOF)

    // reader will be a FileReader over the source file
    public Scanner (Reader reader) {
        // TODO: initialize scanner
        input = new BufferedReader(reader);
        closed = false;
        lineNum = charPos = 1;
        scan = "";
    }

    // signal an error message
    public void Error (String msg, Exception e) {
        System.err.println("Scanner: Line - " + lineNum + ", Char - " + charPos);
        if (e != null) {
            e.printStackTrace();
        }
        System.err.println(msg);
    }

    /*
     * helper function for reading a single char from input
     * can be used to catch and handle any IOExceptions,
     * advance the charPos or lineNum, etc.
     */
    private int readChar () {
        // TODO: implement
        int result = 0;
        try {
            result = input.read();
        } catch (IOException ioe) {
            Error("IOException occurred while reading char", ioe);
        }
        if (result == '\n') {
            lineNum++;
            charPos = 1;
        } else if (result == -1) {
            // do nothing
        } else {
            charPos++;
        }
        return result;
    }

    /*
     * function to query whether or not more characters can be read
     * depends on closed and nextChar
     */
    @Override
    public boolean hasNext () {
        // TODO: implement
        return !closed;
    }

    /*
     *	returns next Token from input
     *
     *  invariants:
     *  1. call assumes that nextChar is already holding an unread character
     *  2. return leaves nextChar containing an untokenized character
     *  3. closes reader when emitting EOF
     */
    @Override
    public Token next () {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        // close reader and return EOF
        if (/* hasNext() && */ (nextChar == -1)) {
            try {
                input.close();
                closed = true;
                return Token.EOF(lineNum, charPos);
            } catch (IOException ioe) {
                Error("IOException occurred while closing reader", ioe);
            }
        }

        // TODO: implement
        // boolean matchOnce = false;
        int startLN = lineNum, startCP = charPos;
        Token t = new Token(null, startLN, startCP);
        scan = "";
        String errLexeme = "";
        // State currentState = State.Q0;
        // Token t = null;
        while (hasNext()) {
            nextChar = readChar();
            // skip comments
            if (nextChar == '/') {
                errLexeme = "/";
                nextChar = readChar();
                // skip over line comment
                if (nextChar == '/') {
                    while (nextChar != '\n' && nextChar != -1) {
                        nextChar = readChar();
                    }
                    // EOL or EOF reached
                    // return any token that came before comment
                    if (scan != "") {
                        return new Token(scan, startLN, startCP);
                    }
                }
                // skip over block/inline comment
                else if (nextChar == '*') {
                    errLexeme += (char) nextChar;
                    while (nextChar != -1) {
                        nextChar = readChar();
                        errLexeme += (char) nextChar;
                        if (nextChar == '*') {
                            nextChar = readChar();
                            if (nextChar == '/' || nextChar == -1) {
                                break;
                            }
                            errLexeme += (char) nextChar;
                        }
                    }
                    if (nextChar == -1) {
                        return new Token(scan + errLexeme, startLN, startCP);
                    }
                } else if (Character.isWhitespace(nextChar)) {
                    return new Token(scan + errLexeme, startLN, startCP);
                } else {
                    errLexeme += (char) nextChar;
                }
            } else if (nextChar == -1) {
                if ((scan + errLexeme) != "") {
                    // System.out.print(scan + errLexeme);
                    return new Token(scan + errLexeme, startLN, startCP);
                }
            } else if (Character.isWhitespace(nextChar)) {
                continue;
            } else {
                scan += (char) nextChar;
            }
        }
        // should never reach this
        return null;
    }

    // OPTIONAL: add any additional helper or convenience methods
    //           that you find make for a cleaner design
    //           (useful for handling special case Tokens)
    
    private void newMark() {
        try {
                input.mark(32);
            } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
