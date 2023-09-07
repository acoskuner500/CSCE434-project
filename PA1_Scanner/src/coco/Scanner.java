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
    boolean marked = false;
    boolean error = false;

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
            // if (Character.isWhitespace(result)) {
            //     // System.out.print((int) result + " ");
            // }
            // String x;
            // switch (result) {
            //     case 9: x = "\\t";
            //     case 10: x = "\\n";
            //     case 11: x = "\\v";
            //     case 12: x = "\\f";
            //     case 13: x = "\\r";
            //     case 32: x = " ";
            //     default: x = "" + (char) result;
            // }
            // // System.out.print(x);
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
        int startLN = lineNum, startCP = charPos;
        Token.State state = Token.State.Q0;
        Token.State commentState;
        boolean isComment = false;
        scan = "";
        String commentBuf = "";
        while (hasNext()) {
            // close reader and return EOF
            if (nextChar == -1) {
                try {
                    input.close();
                    closed = true;
                    return Token.EOF(lineNum, charPos);
                } catch (IOException ioe) {
                    Error("IOException occurred while closing reader", ioe);
                }
            }
            
            nextChar = readChar();
            // System.out.print(nextChar + " ");
            commentState = state;
            state = Token.automaton(state, (char) nextChar);
            // System.out.print(state + " ");
            if (nextChar == '/') {
                commentBuf += (char) nextChar;
                nextChar = readChar();
                if (nextChar == '/') {
                    commentBuf += (char) nextChar;
                    isComment = true;
                    while (nextChar != '\n' && nextChar != -1) {
                        nextChar = readChar();
                        commentBuf += (char) nextChar;
                    }
                } else if (nextChar == '*') {
                    isComment = true;
                    commentBuf += (char) nextChar;
                    while (nextChar != -1) {
                        nextChar = readChar();
                        commentBuf += (char) nextChar;
                        if (nextChar == '*') {
                            nextChar = readChar();
                            switch (nextChar) {
                                case '/': commentBuf += (char) nextChar;
                                case -1: break;
                                default: 
                                    commentBuf += (char) nextChar;
                                    continue;
                            }
                            // if (nextChar == '/' || nextChar == -1) {
                            //     break;
                            // }
                            // commentBuf += (char) nextChar;
                        }
                    }
                    if (nextChar == -1) {
                        if (marked) {
                            try {
                                input.reset();
                                // System.out.println("scan 145: " + scan);
                                return new Token(scan, startLN, startCP);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // System.out.println("scan and comment: " + scan + commentBuf);
                            return new Token(scan + commentBuf, startLN, startCP);
                        }
                    }
                }
            }
            if (Token.invalidState(state) ) {
                if (marked) {
                    try {
                        input.reset();
                        marked = false;
                        // System.out.println("scan 177: " + scan);
                        return new Token(scan, startLN, startCP);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (nextChar == -1) {
                        // System.out.println("scan 184: " + scan);
                        return new Token(scan, startLN, startCP);
                    } else if (Character.isWhitespace(nextChar)) {
                        if (scan != "") {
                            return new Token(scan, startLN, startCP);
                        }
                        state = Token.State.Q0;
                    } else if (isComment) {
                        isComment = false;
                        state = commentState;
                    } else {
                        scanIn(state, nextChar);
                    }
                }
            } else if (isComment) {
                scan = "";
                commentBuf = "";
                state = Token.State.Q0;
                isComment = false;
            } else {
                scanIn(state, nextChar);
            }

            // for (char c : scan.toCharArray()) {
            //     System.out.print((int) c + " ");
            // }
            // if (scan == "") System.out.print("empty string");
            // System.out.println();
        }
        // should never reach this
        // System.out.println("scan 189: " + scan);
        return null;
    }

    // OPTIONAL: add any additional helper or convenience methods
    //           that you find make for a cleaner design
    //           (useful for handling special case Tokens)
    
    private void scanIn(Token.State state, int nChar) {
        scan += (char) nChar;
        if (Token.isAcceptingState(state)) {
            try {
                    input.mark(2048);
                    marked = true;
                    // System.out.print(marked + " ");
                } catch (IOException e) {
                    e.printStackTrace();
            }
        }
    }
}
