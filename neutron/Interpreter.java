import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

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
        }
        return null;

    }

    // For statements;
    @Override
    public Void visitPrintStmt(Stmt.Print print) {
        Object value = evaluate(print.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression expression) {
        evaluate(expression.expression);
        return null;
    }

    // Helper method that will send again the expression inside the group '()'
    // again into interpreter visitor class.
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    // Checks wheather the value is truthy or not
    // for now only nil and false are truthy.
    private boolean isTruthy(Object object) {
        if (object == null) {
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
}
