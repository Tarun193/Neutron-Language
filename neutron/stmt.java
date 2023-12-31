import java.util.List;

abstract class Stmt{
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitBlockStmt(Block stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitIfStmt(If stmt);
    R visitWhileStmt(While stmt);
    R visitForStmt(For stmt);
    R visitBreakStmt(Break stmt);
    R visitContinueStmt(Continue stmt);
    R visitClassStmt(Class stmt);
    R visitReturnStmt(Return stmt);
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
   static class Function extends Stmt {
    Function(Token name, List<Token> params, List<Stmt> body){
    this.name = name;
    this.params = params;
    this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final List<Token> params;
    final List<Stmt> body;
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
   static class While extends Stmt {
    While(Expr condition, Stmt stmtBody){
    this.condition = condition;
    this.stmtBody = stmtBody;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt stmtBody;
  }
   static class For extends Stmt {
    For(Expr codition, Expr runner, Stmt loopBody){
    this.codition = codition;
    this.runner = runner;
    this.loopBody = loopBody;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitForStmt(this);
    }

    final Expr codition;
    final Expr runner;
    final Stmt loopBody;
  }
   static class Break extends Stmt {
    Break(){
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBreakStmt(this);
    }

  }
   static class Continue extends Stmt {
    Continue(){
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitContinueStmt(this);
    }

  }
   static class Class extends Stmt {
    Class(Token name, List<Stmt.Function> methods, List<Stmt.Function> staticMethods, Expr.Variable superClass){
    this.name = name;
    this.methods = methods;
    this.staticMethods = staticMethods;
    this.superClass = superClass;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final Token name;
    final List<Stmt.Function> methods;
    final List<Stmt.Function> staticMethods;
    final Expr.Variable superClass;
  }
   static class Return extends Stmt {
    Return(Token keyword, Expr value){
    this.keyword = keyword;
    this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final Expr value;
  }
}
