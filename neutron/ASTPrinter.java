public class ASTPrinter implements Expr.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return paranthesis(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return paranthesis("grouping", expr.experession);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return paranthesis(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    // These method are just for printing purposes
    // Both print the tree in differnet order one is part of text
    // other is exercise
    private String paranthesis(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("( ").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(" )");

        return builder.toString();
    }

    private String ReversePolishNotationPrinting(String Name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(" )");
        builder.append(Name);

        return builder.toString();
    }

}
