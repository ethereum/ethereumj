package org.ethereum.gui;

import javax.swing.text.Segment;

import org.ethereum.vm.OpCode;
import org.fife.ui.rsyntaxtextarea.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 24/04/14 11:52
 */
public class SerpentTokenMaker extends AbstractTokenMaker {

// http://fifesoft.com/rsyntaxtextarea/doc/CustomSyntaxHighlighting.html

    protected final String operators = ".@:*<>=?|!";

    private int currentTokenStart;
    private int currentTokenType;

    private boolean bracketVariable;            // Whether a variable is of the format %{...}

    /**
     * Constructor.
     */
    public SerpentTokenMaker() {
        super();    // Initializes tokensToHighlight.
    }

    /**
     * Checks the token to give it the exact ID it deserves before
     * being passed up to the super method.
     *
     * @param segment <code>Segment</code> to get text from.
     * @param start Start offset in <code>segment</code> of token.
     * @param end End offset in <code>segment</code> of token.
     * @param tokenType The token's type.
     * @param startOffset The offset in the document at which the token occurs.
     */
    @Override
    public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {

        switch (tokenType) {
            // Since reserved words, functions, and data types are all passed
            // into here as "identifiers," we have to see what the token
            // really is...
            case Token.IDENTIFIER:
                int value = wordsToHighlight.get(segment, start,end);
                if (value!=-1)
                    tokenType = value;
                break;

            case Token.ANNOTATION:
                value = wordsToHighlight.get(segment, start,end);
                if (value!=-1)
                    tokenType = value;
                break;
        }
        super.addToken(segment, start, end, tokenType, startOffset);
    }

    /**
     * Returns the text to place at the beginning and end of a
     * line to "comment" it in a this programming language.
     *
     * @return The start and end strings to add to a line to "comment"
     *         it out.
     */
    @Override
    public String[] getLineCommentStartAndEnd() {
        return new String[] { "#", null };
    }

    /**
     * Returns whether tokens of the specified type should have "mark
     * occurrences" enabled for the current programming language.
     *
     * @param type The token type.
     * @return Whether tokens of this type should have "mark occurrences"
     *         enabled.
     */
    @Override
    public boolean getMarkOccurrencesOfTokenType(int type) {
        return type==Token.IDENTIFIER || type==Token.VARIABLE;
    }

    /**
     * Returns the words to highlight for Windows batch files.
     *
     * @return A <code>TokenMap</code> containing the words to highlight for
     *         Windows batch files.
     * @see org.fife.ui.rsyntaxtextarea.AbstractTokenMaker#getWordsToHighlight
     */
    @Override
    public TokenMap getWordsToHighlight() {

        TokenMap tokenMap = new TokenMap(false); // Ignore case.

        int reservedWord = Token.RESERVED_WORD;
        tokenMap.put("set",     reservedWord);
        tokenMap.put("if",      reservedWord);
        tokenMap.put("else",        reservedWord);
        tokenMap.put("elif",        reservedWord);
        tokenMap.put("seq",     reservedWord);
        tokenMap.put("while",       reservedWord);
        tokenMap.put("byte",        reservedWord);
        tokenMap.put("access",      reservedWord);
        tokenMap.put("arrset",      reservedWord);
        tokenMap.put("set_and_inc",     reservedWord);
        tokenMap.put("array",       reservedWord);
        tokenMap.put("getch",       reservedWord);
        tokenMap.put("setch",       reservedWord);
        tokenMap.put("string",      reservedWord);
        tokenMap.put("send",        reservedWord);
        tokenMap.put("create",      reservedWord);
        tokenMap.put("sha3",        reservedWord);
        tokenMap.put("sha3bytes",       reservedWord);
        tokenMap.put("sload",       reservedWord);
        tokenMap.put("sstore",      reservedWord);
        tokenMap.put("calldataload",        reservedWord);
        tokenMap.put("id",      reservedWord);
        tokenMap.put("return",      reservedWord);
        tokenMap.put("suicide",     reservedWord);

        tokenMap.put("stop",            reservedWord);

        int function = Token.FUNCTION;
        tokenMap.put("msg",                 function);
        tokenMap.put("data",                    function);
        tokenMap.put("contract",            function);
        tokenMap.put("storage",         function);
        tokenMap.put("block",           function);
        tokenMap.put("tx",          function);

        // ALL the assembly tokens
        int reservedWord2 = Token.RESERVED_WORD_2;
        for (OpCode value : OpCode.values()) {
            tokenMap.put(value.name(), reservedWord2);
            tokenMap.put("[asm", reservedWord2);
            tokenMap.put("asm]", reservedWord2);
        }

        int dataType = Token.ANNOTATION;
        tokenMap.put("init",            dataType);
        tokenMap.put("code",            dataType);

        return tokenMap;
    }


    /**
     * Returns a peerInfoList of tokens representing the given text.
     *
     * @param text The text to break into tokens.
     * @param startTokenType The token with which to start tokenizing.
     * @param startOffset The offset at which the line of tokens begins.
     * @return A linked peerInfoList of tokens representing <code>text</code>.
     */
    public Token getTokenList(Segment text, int startTokenType, final int startOffset) {

        resetTokenList();

        char[] array = text.array;
        int offset = text.offset;
        int count = text.count;
        int end = offset + count;

        // See, when we find a token, its starting position is always of the form:
        // 'startOffset + (currentTokenStart-offset)'; but since startOffset and
        // offset are constant, tokens' starting positions become:
        // 'newStartOffset+currentTokenStart' for one less subtraction operation.
        int newStartOffset = startOffset - offset;

        currentTokenStart = offset;
        currentTokenType  = startTokenType;

//beginning:
        for (int i=offset; i<end; i++) {

            char c = array[i];

            switch (currentTokenType) {

                case Token.NULL:

                    currentTokenStart = i;  // Starting a new token here.

                    switch (c) {

                        case '#':
                            currentTokenType = Token.COMMENT_EOL;
                            break;

                        case ' ':
                        case '\t':
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            currentTokenType = Token.ERROR_STRING_DOUBLE;
                            break;


                        // The "separators".
                        case '(':
                        case ')':
                            addToken(text, currentTokenStart,i, Token.SEPARATOR, newStartOffset+currentTokenStart);
                            currentTokenType = Token.NULL;
                            break;

                        // The "separators2".
                        case ',':
                        case ';':
                            addToken(text, currentTokenStart,i, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenType = Token.NULL;
                            break;

                        // Newer version of EOL comments, or a label
                        case ':':
                            // If this will be the first token added, it is
                            // a new-style comment or a label
                            if (firstToken == null) {
                                if (i < end - 1 && array[i + 1] == ':') { // new-style comment
                                    currentTokenType = Token.COMMENT_EOL;
                                }
                                else { // Label
                                    currentTokenType = Token.PREPROCESSOR;
                                }
                            }
                            else { // Just a colon
                                currentTokenType = Token.IDENTIFIER;
                            }
                            break;
                        // Newer version of EOL comments, or a label

                        default:
                            // Just to speed things up a tad, as this will usually be the case (if spaces above failed).
                            if (RSyntaxUtilities.isLetterOrDigit(c) || c=='\\') {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            int indexOf = operators.indexOf(c,0);
                            if (indexOf > -1) {
                                addToken(text, currentTokenStart,i, Token.OPERATOR, newStartOffset+currentTokenStart);
                                currentTokenType = Token.NULL;
                                break;
                            }
                            else {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }
                    } // End of switch (c).
                    break;

                case Token.WHITESPACE:

                    switch (c) {

                        case '/':
                            addToken(text, currentTokenStart,i-1,
                                    Token.COMMENT_EOL, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.COMMENT_EOL;
                            break;
                        case ' ':
                        case '\t':
                            break;  // Still whitespace.

                        case '"':
                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.ERROR_STRING_DOUBLE;
                            break;


                        // The "separators".
                        case '(':
                        case ')':
                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            addToken(text, i,i, Token.SEPARATOR, newStartOffset+i);
                            currentTokenType = Token.NULL;
                            break;

                        // The "separators2".
                        case ',':
                        case ';':
                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            addToken(text, i,i, Token.IDENTIFIER, newStartOffset+i);
                            currentTokenType = Token.NULL;
                            break;

                        // Newer version of EOL comments, or a label
                        case ':':
                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            // If the previous (whitespace) token was the first token
                            // added, this is a new-style comment or a label
                            if (firstToken.getNextToken() == null) {
                                if (i < end - 1 && array[i + 1] == ':') { // new-style comment
                                    currentTokenType = Token.COMMENT_EOL;
                                }
                                else { // Label
                                    currentTokenType = Token.PREPROCESSOR;
                                }
                            }
                            else { // Just a colon
                                currentTokenType = Token.IDENTIFIER;
                            }
                            break;

                        default:    // Add the whitespace token and start anew.
                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;

                            // Just to speed things up a tad, as this will usually be the case (if spaces above failed).
                            if (RSyntaxUtilities.isLetterOrDigit(c) || c=='\\') {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            int indexOf = operators.indexOf(c,0);
                            if (indexOf > -1) {
                                addToken(text, currentTokenStart,i, Token.OPERATOR, newStartOffset+currentTokenStart);
                                currentTokenType = Token.NULL;
                                break;
                            }
                            else {
                                currentTokenType = Token.IDENTIFIER;
                            }
                    } // End of switch (c).
                    break;

                default: // Should never happen
                case Token.IDENTIFIER:

                    switch (c) {

                        case ' ':
                        case '\t':
                            // Check for REM comments.
                            if (i-currentTokenStart==3 &&
                                    (array[i-3]=='r' || array[i-3]=='R') &&
                                    (array[i-2]=='e' || array[i-2]=='E') &&
                                    (array[i-1]=='m' || array[i-1]=='M')) {
                                currentTokenType = Token.COMMENT_EOL;
                                break;
                            }
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.ERROR_STRING_DOUBLE;
                            break;


                        // Should be part of identifiers, but not at end of "REM".
                        case '\\':
                            // Check for REM comments.
                            if (i-currentTokenStart==3 &&
                                    (array[i-3]=='r' || array[i-3]=='R') &&
                                    (array[i-2]=='e' || array[i-2]=='E') &&
                                    (array[i-1]=='m' || array[i-1]=='M')) {
                                currentTokenType = Token.COMMENT_EOL;
                            }
                            break;

//                        case '.':
                        case '_':
                            break;  // Characters good for identifiers.

                        // The "separators".
                        case '(':
                        case ')':
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            addToken(text, i,i, Token.SEPARATOR, newStartOffset+i);
                            currentTokenType = Token.NULL;
                            break;

                        // The "separators2".
                        case ',':
                        case ';':
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            addToken(text, i,i, Token.IDENTIFIER, newStartOffset+i);
                            currentTokenType = Token.NULL;
                            break;

                        default:

                            // Just to speed things up a tad, as this will usually be the case.
                            if (RSyntaxUtilities.isLetterOrDigit(c) || c=='\\') {
                                break;
                            }

                            int indexOf = operators.indexOf(c);
                            if (indexOf>-1) {
                                addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                                addToken(text, i,i, Token.OPERATOR, newStartOffset+i);
                                currentTokenType = Token.NULL;
                                break;
                            }
                            // Otherwise, fall through and assume we're still okay as an IDENTIFIER...
                    } // End of switch (c).
                    break;

                case Token.COMMENT_EOL:

                    if (i + 1 >= array.length)
                        break;

                    char nextC = array[i+1];
                    if (nextC == '/') {

                        i = end - 1;
                        addToken(text, currentTokenStart,i, Token.COMMENT_EOL, newStartOffset+currentTokenStart);
                        // We need to set token type to null so at the bottom we don't add one more token.
                        currentTokenType = Token.NULL;
                    }
                    break;

                case Token.PREPROCESSOR: // Used for labels
                    i = end - 1;
                    addToken(text, currentTokenStart,i, Token.PREPROCESSOR, newStartOffset+currentTokenStart);
                    // We need to set token type to null so at the bottom we don't add one more token.
                    currentTokenType = Token.NULL;
                    break;

                case Token.ERROR_STRING_DOUBLE:

                    if (c == '"') {
                        addToken(text, currentTokenStart,i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset+currentTokenStart);
                        currentTokenStart = i + 1;
                        currentTokenType = Token.NULL;
                    }
                    // Otherwise, we're still an unclosed string...
                    break;

                case Token.VARIABLE:

                    if (i == currentTokenStart + 1) { // first character after '%'.
                        bracketVariable = false;
                        switch (c) {
                            case '{':
                                bracketVariable = true;
                                break;
                            default:
                                if (RSyntaxUtilities.isLetter(c) || c==' ') { // No tab, just space; spaces are okay in variable names.
                                    break;
                                }
                                else if (RSyntaxUtilities.isDigit(c)) { // Single-digit command-line argument ("%1").
                                    addToken(text, currentTokenStart,i, Token.VARIABLE, newStartOffset+currentTokenStart);
                                    currentTokenType = Token.NULL;
                                    break;
                                }
                                else { // Anything else, ???.
                                    addToken(text, currentTokenStart,i-1, Token.VARIABLE, newStartOffset+currentTokenStart); // ???
                                    i--;
                                    currentTokenType = Token.NULL;
                                    break;
                                }
                        } // End of switch (c).
                    }
                    else { // Character other than first after the '%'.
                        if (bracketVariable == true) {
                            if (c == '}') {
                                addToken(text, currentTokenStart, i, Token.VARIABLE, newStartOffset + currentTokenStart);
                                currentTokenType = Token.NULL;
                            }
                        }
                        break;
                    }
                    break;

            } // End of switch (currentTokenType).

        } // End of for (int i=offset; i<end; i++).

        // Deal with the (possibly there) last token.
        if (currentTokenType != Token.NULL) {
            // Check for REM comments.
            if (end-currentTokenStart==3 &&
                    (array[end-3]=='r' || array[end-3]=='R') &&
                    (array[end-2]=='e' || array[end-2]=='E') &&
                    (array[end-1]=='m' || array[end-1]=='M')) {
                currentTokenType = Token.COMMENT_EOL;
            }
            addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
        }
        addNullToken();
        // Return the first token in our linked peerInfoList.
        return firstToken;
    }
}