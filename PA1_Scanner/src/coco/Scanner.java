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
        lineNum = charPos = 0;
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
            charPos = 0;
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
        // if (nextChar == -1) {
            // try {
            //     input.close();
                // closed = true;
            // } catch (IOException ioe) {
            //     Error("IOException occurred while closing reader", ioe);
            // }
        // }
        // return (!closed /* && !Character.isWhitespace(nextChar) */);
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
        if (closed && (nextChar == -1)) {
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
        int endCP = 0;
        Token t = new Token(null, startLN, startCP);
        scan = "";
        // Token t = null;
        while (hasNext()) {
            scan += (nextChar = readChar());
            // skip comments
            if (nextChar == '/') {
                // skip over line comment
                nextChar = readChar();
                if (nextChar == '/') {
                    while (nextChar != '\n' && nextChar != -1) {
                        nextChar = readChar();
                    }
                    if (nextChar == -1) {
                        closed = true;
                    }
                    // if (matchOnce) {
                    //     return t;
                    // }
                }
                // skip over block/inline comment
                else if (nextChar == '*') {
                    while (nextChar != '*' || nextChar != -1) {
                        nextChar = readChar();
                    }
                    if (nextChar == '*') {
                        if (nextChar == '/') {
                            break;
                        }
                    }
                } else if (Character.isWhitespace(nextChar)) {
                    // return new Token()
                }
            } else if (Character.isWhitespace(nextChar)) {

            }
            scan += nextChar;
            t = new Token(scan, startLN, startCP);
            if (!t.kind.matchLexeme("")) {
                // not a special case token
                endCP = startCP + scan.length();
            }
            // matchOnce = !(matchOnce || (t.kind != Token.Kind.ERROR)); // toggle true for first match
        }
        if (nextChar == -1) {
            t = Token.EOF(startLN, startCP);
            closed = true;
        } else {
            nextChar = readChar();
        }
        return t;
    }

    // OPTIONAL: add any additional helper or convenience methods
    //           that you find make for a cleaner design
    //           (useful for handling special case Tokens)
}
