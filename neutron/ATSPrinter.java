public class ATSPrinter implements Expr.Visitor<String> {

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

    public static void main(String[] args) {
        Expr expr = new Expr.Binary(
                new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(123.4)));

        System.out.println(new ATSPrinter().print(expr));
    }
}
