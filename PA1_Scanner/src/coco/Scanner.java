package coco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Scanner implements Iterator<Token> {

    private BufferedReader input;   // buffered reader to read file
    private boolean closed; // flag for whether reader is closed or not

    private int lineNum;    // current line number
    private int charPos;    // character offset on current line

    private String scan;    // current lexeme being scanned in
    private int nextChar;   // contains the next char (-1 == EOF)
    boolean marked = false;
    boolean error = false;

    // reader will be a FileReader over the source file
    public Scanner (Reader reader) {
        // TODO: initialize scanner
        input = new BufferedReader(reader);
        closed = false;
        lineNum = charPos = 0;
        scan = "";
        readChar();
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
        try {
            nextChar = input.read();
        } catch (IOException ioe) {
            Error("IOException occurred while reading char", ioe);
        }
        charPos++;
        if (nextChar == '\n') {
            lineNum++;
            charPos = 0;
        }
        return nextChar;
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

        // TODO: implement
        int prevChar = 0;
        Token.State state, prevState, saveState;
        state = prevState = saveState = Token.State.Q0;
        boolean isLineComment = false;
        boolean isComment = false;
        boolean commentStarSeen = false;
        boolean lexemeFound = false;
        scan = "";
        String commentBuf = "";
        while (hasNext()) {
            // close reader and return EOF
            if (nextChar == -1 && scan.length() == 0) {
                try {
                    input.close();
                    closed = true;
                    return Token.EOF(lineNum, charPos);
                } catch (IOException ioe) {
                    Error("IOException occurred while closing reader", ioe);
                }
            }

            if (isLineComment) {
                if (nextChar == '\n' || nextChar == -1) {
                    isLineComment = false;
                    scan = "";
                    prevChar = 0;
                    state = prevState = Token.State.Q0;
                }
                readChar();
                continue;
            }
            if (isComment) {
                if (nextChar == -1) {
                    scan = commentBuf;
                    break;
                }
                commentBuf += (char) nextChar;
                if (nextChar == '*') {
                    commentStarSeen = true;
                } else if (commentStarSeen) {
                    if (nextChar == '/') {
                        commentBuf = "";
                        isComment = false;
                        prevChar = 0;
                        state = saveState;
                        prevState = saveState = Token.State.Q0;
                        scan = scan.substring(0, scan.length() - 1);
                    }
                    commentStarSeen = false;
                }
                readChar();
                continue;
            }
            if (prevChar == '/') {
                if (nextChar == '/' && !isLineComment) {
                    isLineComment = true;
                    scan = scan.substring(0, scan.length() - 1);
                    lexemeFound = (scan.length() > 0);
                } else if (nextChar == '*' && !isComment) {
                    isComment = true;
                    scan = scan.substring(0, scan.length() - 1);
                    commentBuf = scan + "/*";
                }
            }

            prevState = state;
            state = Token.automaton(state, (char) nextChar);
            if (nextChar == '/' && !(isComment || isLineComment)) {
                saveState = prevState;
            }
            if (scan.length() == 0) {
                if (Character.isWhitespace(nextChar)) {
                    readChar();
                    state = prevState = Token.State.Q0;
                    continue;
                }
            } else {
                if (nextChar == -1 || Character.isWhitespace(nextChar)) {
                    lexemeFound = true;
                }
                switch (state) {
                    case Q0:
                    case Q95:
                    case Q114:
                        lexemeFound = false;
                        break;
                    case Q116:
                        if (!illegals.contains((char) nextChar) && prevState != Token.State.Q116) {
                            lexemeFound = true;
                        }
                        if (Token.isFinalState(prevState) ||
                        (Token.isTransientState(prevState) && prevState != Token.State.Q113 && prevState != Token.State.Q115)) {
                            lexemeFound = true;
                        }
                    default:
                }
            }
            if (lexemeFound) break;
            prevChar = nextChar;
            scan += (char) nextChar;
            readChar();
        }
        return new Token(scan, lineNum, charPos);
    }

    // OPTIONAL: add any additional helper or convenience methods
    //           that you find make for a cleaner design
    //           (useful for handling special case Tokens)
    private List<Character> illegals = Arrays.asList(
        '~', '`', '@', '#', '$', '&', '|', '\\', '\'', '\"', '?'
    );
}
