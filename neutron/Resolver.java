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

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

}
