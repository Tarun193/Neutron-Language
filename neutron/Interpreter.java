public class Interpreter implements Expr.Visitor<Object> {

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
                return (Double) left > (Double) right;

            case GREATER_EQUAL:
                return (Double) left >= (Double) right;

            case LESS:
                return (Double) left < (Double) right;

            case LESS_EQUAL:
                return (Double) left <= (Double) right;

            // Arthimatic operators;
            case MINUS:
                return (Double) left - (Double) right;
            case SLASH:
                return (Double) left / (Double) right;
            case STAR:
                return (Double) left / (Double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left + (Double) right;
                } else if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                break;

            // Equality operators
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        return null;

    }

    // Helper method that will send again the expression inside the group '()'
    // again into interpreter visitor class.
    private Object evaluate(Expr expr) {
        return expr.accept(this);
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

}
