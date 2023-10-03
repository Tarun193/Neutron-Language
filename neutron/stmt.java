import java.util.List;

abstract class Stmt{
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitBlockStmt(Block stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitIfStmt(If stmt);
  }

  abstract <R> R accept(Visitor<R> visitor);
   static class Expression extends Stmt {
    Expression(Expr expression){
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
   static class Block extends Stmt {
    Block(List<Stmt> statements){
    this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
   static class Print extends Stmt {
    Print(Expr expression){
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }
   static class Var extends Stmt {
    Var(List<Token> names, List<Expr> initializers){
    this.names = names;
    this.initializers = initializers;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final List<Token> names;
    final List<Expr> initializers;
  }
   static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch){
    this.condition = condition;
    this.thenBranch = thenBranch;
    this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
}
