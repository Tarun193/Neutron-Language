import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreater;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunctionType = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreater = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER,
        STATICMETHOD
    }

    private enum ClassType {
        None,
        CLASS,
        SUBCLASS
    }

    private ClassType currentEnclosingClass = ClassType.None;

    // Method for resolving a block;
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        for (Token name : stmt.names) {
            declare(name);
        }
        if (stmt.initializers.size() != 0) {
            for (Expr initializer : stmt.initializers) {
                resolve(initializer);
            }
        }
        for (Token name : stmt.names) {
            define(name);
        }
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            neutron.error(expr.name, "can't read local variable in it's own initializer");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    // Visiting assignment Expression
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    // Resolving function declaration
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    // resolving expression Statements
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    // Resolving id statements;
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null)
            resolve(stmt.elseBranch);
        return null;
    }

    // resolving print Stmts
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    // Resolving while Stmts.
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.stmtBody);
        return null;
    }

    // Resolving for statements
    @Override
    public Void visitForStmt(Stmt.For stmt) {
        resolve(stmt.codition);
        resolve(stmt.runner);
        resolve(stmt.loopBody);
        return null;
    }

    // Resolving resturn Stmts;
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunctionType == FunctionType.NONE) {
            neutron.error(stmt.keyword, "Can't return from top level-code");
        }
        if (stmt.value != null) {
            if (currentFunctionType == FunctionType.INITIALIZER) {
                neutron.error(stmt.keyword, "Can't return from constructor");
            }
            resolve(stmt.value);
        }

        return null;
    }

    // Resolving binary expression.
    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    // Resolving method calls
    public Void visitCallExpr(Expr.Call calle) {
        resolve(calle.calle);

        for (Expr arguments : calle.arguments) {
            resolve(arguments);
        }
        return null;
    }

    // Resolving Grouping Expression
    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.experession);
        return null;
    }

    // Resolving Literal Expression
    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    // Resolving Logical Expressions
    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    // resolving Unary expression
    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    // Resloving Lambda Expression
    @Override
    public Void visitLambdaExpr(Expr.Lambda lambda) {
        beginScope();
        for (Token param : lambda.params) {
            declare(param);
            define(param);
        }
        resolve(lambda.expr);
        endScope();
        return null;
    }

    // Resolving Break and continue statement;
    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        return null;
    }

    @Override
    public Void visitTerneryExpr(Expr.Ternery expr) {
        resolve(expr.condition);
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.name);
        define(stmt.name);

        if (stmt.superClass != null && stmt.name.lexeme.equals(stmt.superClass.name.lexeme)) {
            neutron.error(stmt.name, "A class cannot inherit from itself");
        }

        ClassType enclosingClass = currentEnclosingClass;
        currentEnclosingClass = ClassType.CLASS;

        if (stmt.superClass != null) {
            currentEnclosingClass = ClassType.SUBCLASS;
            resolve(stmt.superClass);
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);
        for (Stmt.Function method : stmt.methods) {
            resolveFunction(method, currentFunctionType.METHOD);
        }
        for (Stmt.Function method : stmt.staticMethods) {
            resolveFunction(method, currentFunctionType.STATICMETHOD);
        }
        endScope();

        // As we started scope only if the super class exists in the same why we ended
        // scope.
        if (stmt.superClass != null)
            endScope();
        currentEnclosingClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        // An properties are dynamic that why we are not resolving them.
        resolve(expr.Object);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.Object);
        resolve(expr.value);
        return null;
    }

    // Resolving this expression;
    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentFunctionType == FunctionType.STATICMETHOD) {
            neutron.error(expr.Keyword, "Cannot refer to instance properties in a static method");
        }
        if (currentEnclosingClass == ClassType.None) {
            neutron.error(expr.Keyword, "Cannot use 'this' outside any class.");
        }
        resolveLocal(expr, expr.Keyword);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        
        if (currentEnclosingClass == ClassType.None) {
            neutron.error(expr.Keyword, "Can't use super outside the class");
        }
        else if(currentEnclosingClass != ClassType.SUBCLASS){
            neutron.error(expr.Keyword, "Can't use 'super' in a class with super class");
        }
        resolveLocal(expr, expr.Keyword);
        return null;
    }

    // -------- Utility Methods ----------------

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    // Resolving a statement, this method will call visitor method associated with
    // passed statement.
    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    // Same as for expressions
    private void resolve(Expr expr) {
        expr.accept(this);
    }

    // Stating a scope
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    // Ending a scope
    private void endScope() {
        scopes.pop();
    }

    // Handling declaration of variables
    private void declare(Token name) {
        if (scopes.isEmpty())
            return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            neutron.error(name, "Already a variable declared in this scope.");
        }
        scopes.peek().put(name.lexeme, false);
    }

    // Handling defination of variable;
    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreater.resolve(expr, scopes.size() - 1 - i);
            }
        }
    }

    // Helper method to resolve function declaration
    private void resolveFunction(Stmt.Function stmt, FunctionType functionType) {
        beginScope();
        FunctionType enclosingFunctionType = currentFunctionType;
        currentFunctionType = functionType;
        if (stmt.name.lexeme.equals("init")) {
            currentFunctionType = FunctionType.INITIALIZER;
        }
        for (Token param : stmt.params) {
            declare(param);
            define(param);
        }

        resolve(stmt.body);
        endScope();
        this.currentFunctionType = enclosingFunctionType;
    }

}
