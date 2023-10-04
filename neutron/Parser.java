import java.util.ArrayList;
import java.util.List;

class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        try {
            List<Stmt> statements = new ArrayList<>();
            while (!isAtEnd()) {
                statements.add(declaration());
            }
            return statements;
        } catch (Exception e) {
            return null;
        }
    }
    /*
     * GRAMMER RULES:
     * expression → assignment ;
     * assignment → IDENTIFIER "=" assignment
     * | logic_or;
     * logic_or -> logic_and ( "or" logic_and)*;
     * logic_and -> equality ("and" equality)*;
     * equality → comparison ( ( "!=" | "==" ) comparison )* ;
     * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     * term → factor ( ( "-" | "+" ) factor )* ;
     * factor → unary ( ( "/" | "*" ) unary )* ;
     * unary → ( "!" | "-" ) unary | primary ;
     * primary → NUMBER | STRING | "true" | "false" | "nil"
     * | "(" expression ")" | IDENTIFIER ;
     * 
     * ------------- Rules for statements ---------------
     * 
     * program → declaration* EOF ;
     * block → "{" declaration* "}" ;
     * declaration → varDecl | statement ;
     * varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;
     * statement → exprStmt | printStmt | block | ifStmt | whileStmt;
     * ifStmt → "if" "(" expression ")" statement
     * ( "else" statement )? ;
     * 
     * whileStmt → "while" "(" expression ")" statement ;
     */

    // For handling expression grammer, it straight forward as it expands equality
    // rule;
    // expression → equality;

    // Each method here is creating a expression sub-tree and returns it to it's
    // caller
    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "invalid assign target");
        }
        return expr;
    }

    // For logic_or -> logic_and ( "or" logic_and)*;
    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = expression();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    // For logic_and -> comma ( "or" comma)*;
    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = expression();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
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
            return new Expr.Unary(opreator, right);
        }

        return primary();
    }

    // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" |
    // IDENTIFIER ;

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
            return new Expr.Grouping(expr);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        throw error(peek(), "Not expected expression");
    }

    // Function for Declaration rule;
    // declaration → varDecl | statement ;
    private Stmt declaration() {
        try {
            if (match(TokenType.VAR))
                return varDeclaration();
            return statement();
        } catch (ParseError e) {
            // if any exception occurs what we will do we try to find a syncronization
            // So that parser a parse again from that point
            synchronize();
            return null;
        }
    }

    // Stmt -> printStmt | exprStmt;
    private Stmt statement() {
        // For While loop;
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        // For conditional statements
        if (match(TokenType.IF))
            return ifStatement();
        // For print statements
        if (match(TokenType.PRINT))
            return printStatement();
        // For block statements
        if (match(TokenType.LEFT_BRACE))
            return blockStatement();
        return expressionStatement();
    }

    // exprStmt → print expression ";" ;
    private Stmt printStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Excpected ; after value");
        return new Stmt.Print(expr);
    }

    // block → "{" declaration* "}" ;
    private Stmt blockStatement() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block");
        return new Stmt.Block(statements);
    }

    // exprStmt → expression ";" ;
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Excpected ; after value");
        return new Stmt.Expression(expr);
    }

    // it is an L-value expression.
    // varDecl -> "var" IDENTIFIER ("=" expression)? ";";
    private Stmt varDeclaration() {
        List<Token> names = new ArrayList<>();
        List<Expr> initializer = new ArrayList<>();

        names.add(consume(TokenType.IDENTIFIER, "Expected variable name"));
        while (match(TokenType.COMMA)) {
            names.add(consume(TokenType.IDENTIFIER, "Expected variable name"));
            initializer.add(new Expr.Literal(null));
        }
        if (match(TokenType.EQUAL)) {
            initializer.add(0, expression());
            int i = 1;
            while (match(TokenType.COMMA)) {
                initializer.add(i, expression());
                i++;
            }
        }
        consume(TokenType.SEMICOLON, "Expected ';' after value");
        return new Stmt.Var(names, initializer);
    }

    // ifStmt → "if" "(" expression ")" statement(block) ("else"statement(block))?;
    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after if");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // whileStmt → "while" "(" expression ")" statement ;
    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after while");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition");
        Stmt stmtBody = statement();
        return new Stmt.While(condition, stmtBody);
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
    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    // Calling error method
    private ParseError error(Token token, String message) {
        neutron.error(token, message);
        return new ParseError();
    }

    // Method for synchronizing the parser:
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case VAR:
                case FUN:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }

    }

}