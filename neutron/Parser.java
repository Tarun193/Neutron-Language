import java.util.List;

class Parser {

    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /*
     * GRAMMER RULES:
     * expression → equality ;
     * equality → comparison ( ( "!=" | "==" ) comparison )* ;
     * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     * term → factor ( ( "-" | "+" ) factor )* ;
     * factor → unary ( ( "/" | "*" ) unary )* ;
     * unary → ( "!" | "-" ) unary | primary ;
     * primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
     */

    // For handling expression grammer, it straight forward as it expands equality
    // rule;
    // expression → equality;

    // Each method here is creating a expression sub-tree and returns it to it's
    // caller
    private Expr expression() {
        return equality();
    }

    // equality → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.kes, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // term → factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor → unary ( ( "/" | "*" ) unary )* ;
    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token oprator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, oprator, right);
        }
        return expr;
    }

    // unary → ( "!" | "-" ) unary | primary ;
    private Expr unary() {

        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token opreator = previous();
            Expr right = unary();
            Expr expr = new Expr.Unary(opreator, right);
        }

        return primary();
    }

    // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;

    private Expr primary() {
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        else if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        else if (match(TokenType.NIL))
            return new Expr.Literal(null);
        else if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");
            expr = new Expr.Grouping(expr);
        }
    }

    // Utility methods;
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    // to check the type of current token is equals to passed type.
    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    // just consume a token and resturns it.
    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    // To check wheather we are not at end of a file token
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    // Returns the current token.
    private Token peek() {
        return tokens.get(current);
    }

    // return the most recent token we just consumed
    private Token previous() {
        return tokens.get(current - 1);
    }

    // for consuming Right Parenthiese ')';
    private Token consume(TokenType type, String message){
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    // Calling error method
    private ParseError error(Token token, String message){
        neutron.error(token, message);
        return new ParseError();
    }

}