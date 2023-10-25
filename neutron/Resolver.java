import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreater;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter) {
        this.interpreater = interpreter;
    }

    // Method for resolving a block;
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        reslove(stmt.statements);
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
                reslove(initializer);
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
        reslove(expr);
        resolveLocal(expr, expr.name);
        return null;
    }
    // -------- Utility Methods ----------------

    void reslove(List<Stmt> statements) {
        for (Stmt statement : statements) {
            reslove(statement);
        }
    }

    // Resolving a statement, this method will call visitor method associated with
    // passed statement.
    private void reslove(Stmt statement) {
        statement.accept(this);
    }

    // Same as for expressions
    private void reslove(Expr expr) {
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
        scopes.peek().put(name.lexeme, false);
    }

    // Handling defination of variable;
    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i++) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreater.resolve(expr, scopes.size() - 1 - i);
            }
        }
    }
}
