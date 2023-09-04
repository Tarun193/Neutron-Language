import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    /*
     * Start point to the first character in the lexeme being scanned,
     * while current points at the charcter being considered.
     * 
     */
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
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS_EQUAL);
            case '>':
                addToken((match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER));
            case '=':
                addToken(match('=') ? TokenType.EQUAL : TokenType.EQUAL);

                // It is a special case as it '//' means comments where as '/' means division
                // operator.
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }

                // Cases for white spaces newline;
            case '\t':
            case '\r':
            case ' ':
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

    private void addToken(TokenType type, Object litereral) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, litereral, line));
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

}
