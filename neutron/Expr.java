import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);

    R visitGroupingExpr(Grouping expr);

    R visitLiteralExpr(Literal expr);

    R visitUnaryExpr(Unary expr);

    R visitVariableExpr(Variable expr);

    R visitLogicalExpr(Logical expr);

    R visitAssignExpr(Assign expr);

    R visitCallExpr(Call expr);

    R visitLambdaExpr(Lambda expr);

    R visitTerneryExpr(Ternery expr);

    R visitGetExpr(Get expr);
  }

  abstract <R> R accept(Visitor<R> visitor);

  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Grouping extends Expr {
    Grouping(Expr experession) {
      this.experession = experession;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr experession;
  }

  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }

  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }

  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }

  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
  }

  static class Call extends Expr {
    Call(Expr calle, Token paren, List<Expr> arguments) {
      this.calle = calle;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr calle;
    final Token paren;
    final List<Expr> arguments;
  }

  static class Lambda extends Expr {
    Lambda(List<Token> params, Expr expr) {
      this.params = params;
      this.expr = expr;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLambdaExpr(this);
    }

    final List<Token> params;
    final Expr expr;
  }

  static class Ternery extends Expr {
    Ternery(Expr condition, Expr left, Expr right) {
      this.condition = condition;
      this.left = left;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTerneryExpr(this);
    }

    final Expr condition;
    final Expr left;
    final Expr right;
  }

  static class Get extends Expr {
    Get(Expr Object, Token name) {
      this.Object = Object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }

    final Expr Object;
    final Token name;
  }
}
