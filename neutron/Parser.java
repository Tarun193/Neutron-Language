import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat.Type;

class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current;
    private int loopDepth = 0;

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
     * unary → ( "!" | "-" ) unary | call ;
     * call → primary ( "(" arguments? ")" )*;
     * arguments → experssion ("," expression)*;
     * primary → NUMBER | STRING | "true" | "false" | "nil"
     * | "(" expression ")" | IDENTIFIER ;
     * 
     * ------------- Rules for statements ---------------
     * 
     * program → declaration* EOF ;
     * block → "{" declaration* "}" ;
     * declaration → funDecl | varDecl | statement ;
     * funDecl -> "fun" function;
     * function -> IDENTIFIER "("parameters?")" block;
     * parameter -> INDENTIFIER ("," INDENTIFIER)*;
     * varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;
     * statement → exprStmt |
     * printStmt |
     * block |
     * ifStmt |
     * whileStmt |
     * forStmt |
     * returnStmt;
     * 
     * ifStmt → "if" "(" expression ")" statement ( "else" statement )? ;
     * whileStmt → "while" "(" expression ")" statement ;
     * The statement always ends with semicolan that's why I didn't ';' after
     * (varDecl | exprStmt | ";")
     * forStmt -> "for" "(" (varDecl | exprStmt | ";") experssion? ";" experssion
     * ")" statement;
     * 
     * returnStmt -> "return" expression? ";";
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

    // For logic_and -> equality ( "or" equality)*;
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

    // factor → unary ( ( "/" | "*" | "%") unary )* ;
    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR, TokenType.MODULUS)) {
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

        return call();
    }

    // call → primary ( "(" arguments? ")" )*;
    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                Expr argument = expression();
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(argument);
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments");
        return new Expr.Call(callee, paren, arguments);
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
            // For Return Stmts
            if (match(TokenType.RETURN))
                return returnStmt();
            // For function definations
            if (match(TokenType.FUN))
                return function("function");
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

        // For Break Stmt;
        if (match(TokenType.BREAK)) {
            return breakStmt();
        }

        // For continue Stmt;
        if (match(TokenType.CONTINUE)) {
            return continueStmt();
        }
        // For for loop;
        if (match(TokenType.FOR)) {
            return forStmt();
        }
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
            return new Stmt.Block(blockStatement());
        return expressionStatement();
    }

    // exprStmt → print expression ";" ;
    private Stmt printStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Excpected ; after value");
        return new Stmt.Print(expr);
    }

    // block → "{" declaration* "}" ;
    private List<Stmt> blockStatement() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block");
        return statements;
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
        try {
            loopDepth++;
            consume(TokenType.LEFT_PAREN, "Expected '(' after while");
            Expr condition = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after condition");
            Stmt stmtBody = statement();
            return new Stmt.While(condition, stmtBody);
        } finally {
            loopDepth--;
        }
    }

    // forStmt -> "for" "(" (varDecl | exprStmt | ";") experssion? ";" experssion
    // ")" statement;
    private Stmt forStmt() {
        try {
            loopDepth++;
            consume(TokenType.LEFT_PAREN, "Expected '(' after for");

            // Initializer for the loop;
            Stmt initializer;
            if (match(TokenType.SEMICOLON)) {
                initializer = null;
            } else if (match(TokenType.VAR)) {
                initializer = varDeclaration();
            } else {
                initializer = expressionStatement();
            }
            // Condition for the loop;
            Expr condition = null;
            if (!check(TokenType.SEMICOLON)) {
                condition = expression();
            }
            consume(TokenType.SEMICOLON, "Expected ';' after loop condition");
            //
            Expr iterator = null;
            if (!check(TokenType.RIGHT_PAREN)) {
                iterator = expression();
            }
            consume(TokenType.RIGHT_PAREN, "Expected ')' after loop iterator");

            Stmt body = statement();
            if (iterator != null) {
                body = new Stmt.Block(
                        Arrays.asList(
                                body,
                                new Stmt.Expression(iterator)));
            }
            // now body = { for loop body; iterator; }

            if (condition == null)
                condition = new Expr.Literal(true);
            body = new Stmt.While(condition, body);
            // now body = while(condition){ for loop body; iterator; }

            if (initializer != null) {
                body = new Stmt.Block(Arrays.asList(initializer, body));
            }
            // now body = {initializer; while(condition){ for loop body; iterator; } }
            // So for loop is an syntatic sugar over while loop.
            return body;
        } finally {
            loopDepth--;
        }

    }

    // For Parsing break statement
    private Stmt breakStmt() {
        if (loopDepth == 0) {
            error(previous(), "cannot use a break stmt outside the loop.");
        }
        consume(TokenType.SEMICOLON, "';' expected after break");
        return new Stmt.Break();
    }

    // // For Parsing continue statement
    private Stmt continueStmt() {
        if (loopDepth == 0) {
            error(previous(), "cannot use a continue ststement outside the loop.");
        }
        consume(TokenType.SEMICOLON, "';' expected after continue");
        return new Stmt.Continue();
    }

    // For return stmts;
    private Stmt returnStmt() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Expecped ';' after expression");
        return new Stmt.Return(keyword, value);
    }

    // For parsing function declaration;
    private Stmt function(String Kind) {
        // consuming function name;
        Token name = consume(TokenType.IDENTIFIER, "Expected " + Kind + "name.");
        consume(TokenType.LEFT_PAREN, "Expected '(' after" + Kind + "name.");
        List<Token> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                params.add(consume(TokenType.IDENTIFIER, "Expected paramater name"));

            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after " + Kind + "parameters");

        consume(TokenType.LEFT_BRACE, "Expected '{' before " + Kind + "body");

        List<Stmt> body = blockStatement();

        return new Stmt.Function(name, params, body);
    }

    // ------------- Utility methods -----------------------;
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