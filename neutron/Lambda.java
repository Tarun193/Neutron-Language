import java.util.List;

class Lambda implements neutronCallable {
    private final Expr.Lambda expr;
    private final Enviornment closure;

    Lambda(Expr.Lambda expr, Enviornment closure) {
        this.expr = expr;
        this.closure = closure;
    }

    // Call method to create new enviorment for each function call
    // and executing it's code using interperater execute block method.
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Enviornment enviornment = new Enviornment(this.closure);
        for (int i = 0; i < expr.params.size(); i++) {
            enviornment.define(expr.params.get(i), arguments.get(i));
        }
        try {
            // System.out.println(interpreter.evaluate(expr, enviornment));
            return interpreter.evaluate(expr.expr, enviornment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
    }

    // Method for return the lenght of parameters.
    @Override
    public int arity() {
        return expr.params.size();
    }

    @Override
    public String toString() {
        return "<Lambda Expr " + expr + " >";
    }
}