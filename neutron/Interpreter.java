import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

    // When we are encountring break we gonna throw this exception then we will
    // handle this exception in while
    private static class BreakException extends RuntimeException {
    }

    // here enviornment changes when ever the scope changes,
    // where as global keeps a fixed reference to the outermost scope.
    final Enviornment global = new Enviornment();
    private Enviornment enviornment = global;
    private final Map<Expr, Integer> locals = new HashMap<>();

    // Flag for contiune Statement
    private boolean Continue = false;

    Interpreter() {
        global.define("clock", new neutronCallable() {

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (Double) (System.currentTimeMillis() / 1000.00);
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpreter(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }

        } catch (RuntimeError error) {
            neutron.runtimeError(error);
        }

    }

    @Override
    public Object visitLiteralExpr(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping group) {
        return evaluate(group.experession);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary unary) {
        Object right = evaluate(unary.right);

        switch (unary.operator.type) {
            case MINUS:
                checkNumberOperand(unary.operator, right);
                return -(Double) right;

            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary binary) {
        // Evaluating left and right oparand
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        // Then preforming operation based on the specific binary operator.binary

        switch (binary.operator.type) {
            // Comparison operators;
            case GREATER:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left > (Double) right;
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) == 1 ? true : false;
                }
                throw new RuntimeError(binary.operator, "Both the operands must be Numbers or Strings");

            case GREATER_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left >= (Double) right;
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) == -1 ? false : true;
                }
                throw new RuntimeError(binary.operator, "Both the operands must be Numbers or Strings");

            case LESS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left < (Double) right;
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) == -1 ? true : false;
                }
                throw new RuntimeError(binary.operator, "Both the operands must be Numbers or Strings");

            case LESS_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left <= (Double) right;
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) == 1 ? false : true;
                }
                throw new RuntimeError(binary.operator, "Both the operands must be Numbers or Strings");

            // Arthimatic operators;
            case MINUS:
                checkNumberOperand(binary.operator, left, right);
                return (Double) left - (Double) right;
            case SLASH:
                checkNumberOperand(binary.operator, left, right);
                if ((Double) right == 0) {
                    throw new RuntimeError(binary.operator, "Divide by 0 is not allowed");
                }
                return (Double) left / (Double) right;
            case STAR:
                checkNumberOperand(binary.operator, left, right);
                return (Double) left * (Double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left + (Double) right;
                } else if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(binary.operator, "Operands must be two numbers or two strings");

            // Equality operators
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);

            case MODULUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left % (Double) right;
                }
                throw new RuntimeError(binary.operator, "invalid operands");
        }
        return null;

    }

    @Override
    public Object visitLogicalExpr(Expr.Logical logical) {
        Object left = evaluate(logical.left);

        if (logical.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(logical.right);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable variable) {
        return lookUpVariable(variable.name, variable);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign assign) {
        Object value = evaluate(assign.value);
        enviornment.assign(assign.name, value);
        return null;
    }

    // Visiting function calls;
    @Override
    public Object visitCallExpr(Expr.Call call) {
        Object callee = evaluate(call.calle);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : call.arguments) {
            arguments.add(evaluate(argument));
        }
        // checking whather the evaluated calle is a callable object or not.
        if (!(callee instanceof neutronCallable)) {
            throw new RuntimeError(call.paren, "Can only call functions and classes");
        }
        neutronCallable function = (neutronCallable) callee;
        // matching the number if arguments with number of parameters for error.
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(call.paren,
                    "Expected " + function.arity() + " arguments but got " +
                            arguments.size() + " arguments");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda lambda) {
        return new Lambda(lambda, enviornment);
    }

    @Override
    public Object visitFunctionStmt(Stmt.Function stmt) {
        neutronCallable function = new neutronFunction(stmt, enviornment);
        enviornment.define(stmt.name, function);
        return null;
    }

    // Statements;
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // Object value = null;
        for (int i = 0; i < stmt.names.size(); i++) {
            enviornment.define(stmt.names.get(i), evaluate(stmt.initializers.get(i)));
        }
        return null;
    }

    // For statements;
    // Handing Print Statments
    @Override
    public Void visitPrintStmt(Stmt.Print print) {
        Object value = evaluate(print.expression);
        System.out.println(stringify(value));
        return null;
    }

    // Handling expression statements
    @Override
    public Void visitExpressionStmt(Stmt.Expression expression) {
        Object value = evaluate(expression.expression);
        if (expression.expression instanceof Expr.Assign || expression.expression instanceof Expr.Call) {
            return null;
        }
        System.out.println(stringify(value));
        return null;
    }

    // Handling block statements
    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        executeBlock(block.statements, new Enviornment(enviornment));
        return null;
    }

    // Handling if statements;
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    // Handling while statements;
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruthy(evaluate(stmt.condition))) {
                if (Continue) {
                    Continue = false;
                }
                execute(stmt.stmtBody);
            }
        } catch (BreakException e) {
        }
        return null;

    }

    // For handling For loop;
    @Override
    public Void visitForStmt(Stmt.For stmt) {
        try {
            while (isTruthy(evaluate(stmt.codition))) {
                if (Continue) {
                    Continue = false;
                }
                execute(stmt.loopBody);
                evaluate(stmt.runner);
            }
        } catch (BreakException e) {
        }
        return null;
    }

    // Method for interpereting break statements
    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    // Method for interpereting Continue statements
    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        Continue = true;
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new Return(value);
    }

    // Resolve method for resolving depth which we are getting from semantic
    // analysis
    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    // -------------- utility methods -------------------

    // Helper method that will send again the expression inside the group '()'
    // again into interpreter visitor class.

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    public Object evaluate(Expr expr, Enviornment enviornment) {
        Enviornment previous = enviornment;
        try {
            this.enviornment = enviornment;
            return expr.accept(this);
        } catch (Exception e) {
            this.enviornment = previous;
        }
        return null;
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    public void executeBlock(List<Stmt> statements, Enviornment enviornment) {

        /*
         * To stores the value of current enviornment, so that we can restore
         * to current enviornment after execution of block code
         */
        Enviornment previous = this.enviornment;
        try {
            this.enviornment = enviornment;
            for (Stmt statement : statements) {
                if (Continue) {
                    continue;
                }
                execute(statement);
            }
        }
        /*
         * we restored the enviornment in finally as it is definatly going to happen
         * even if some error occured
         */

        finally {
            this.enviornment = previous;
        }
    }

    // Checks wheather the value is truthy or not
    // for now only nil and false are truthy.
    private boolean isTruthy(Object object) {
        if (object == null || (object instanceof Double && (Double) object == 0)) {
            return false;
        }
        if (object instanceof Boolean)
            return (Boolean) object;
        return true;
    }

    // Checks two objects are equal or not;
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    // methods for checking weather a operands are number or not
    private void checkNumberOperand(Token operator, Object right) {
        if (right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (right instanceof Double && left instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be a numbers");
    }

    // Method for converting a value to string so that interpreater can print that
    // on screen;
    private String stringify(Object value) {
        if (value == null)
            return "nil";

        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return value.toString();
    }

    // method for looking up for variables according to the resolution;
    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return enviornment.getAt(distance, name.lexeme);
        }
        return global.get(name);
    }
}
