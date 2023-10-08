enum TokenType {
    // SINGLE-CHARACTER TOKENS.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    COLON, MODULUS,

    // ONE OR TWO CHARACTER TOKENS
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // kEYWORDS
    AND, CLASS, ELSE, FALSE, TRUE, FUN, FOR, IF,
    NIL, OR, PRINT, RETURN, SUPER, THIS, VAR, WHILE, BREAK,

    // Literals
    STRING, NUMBER, IDENTIFIER,

    EOF

}