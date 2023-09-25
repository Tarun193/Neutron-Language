import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    /*
     * Hashmap for storing keyword strings mapped with keywork tokentype,
     * as static block get executed when class is loaded into memory.
     */

    /*
     * Start point to the first character in the lexeme being scanned,
     * while current points at the charcter being considered.
     */
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    // Method to scan the all the tokens
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;

            // Handling opreators with mutliple caracters.
            case '!':
                addToken((match('=') ? TokenType.BANG_EQUAL : TokenType.BANG));
                break;

            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS_EQUAL);
                break;

            case '>':
                addToken((match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER));
                break;

            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            // It is a special case as it '//' means comments where as '/' means division
            // operator.
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Cases for white spaces newline;
            case '\t':
            case '\r':
            case ' ':
                break;

            case '\n':
                line++;
                break;
            // Handling string litreals;
            case '"':
                string();
                break;
            // as the language is going to support both single qoute and duble quote
            // strings.
            case '\'':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    neutron.error(line, "Unexcpected character.");
                }

        }
    }

    // utility functions for scan tokens
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object litereal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, litereal, line));
    }

    // for matching the new character.

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        else if (source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    // Peek it is same as advanc but in this we are just reading the current
    // charcter not consuming it.
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    // for creating a string litreal token;
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            neutron.error(line, "Unterminated String.");
        }

        // to consume '"' string terminating character;
        advance();

        /*
         * Here author in the text did substring, other way I will try creating an empty
         * string,
         * and start concaneting characters along the way while travesing string.
         */
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    /* method for checking weather a given given character is digit or not */
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /*
     * Method same as string, where this method consumes and create a
     * number literal token.
     */
    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        if (peek() == '.' && isDigit(peekNext())) {
            // Consuming the decimal point.
            advance();
            // Consuming all the number after decimal point.
            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        // if the source code is finsihed
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    // method for checking given character contains only alphabets and underscore

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'a' && c <= 'z') || (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    /*
     * Function for consuming IDENTIFIERS and checking that identifier is a keyword.
     */

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType tokentype = keywords.get(text);
        if (tokentype == null)
            tokentype = TokenType.IDENTIFIER;

        addToken(tokentype);

    }
}
