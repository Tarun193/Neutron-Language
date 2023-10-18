import java.util.List;

// Wrapper class around the function stmt to handle function calls
public class neutronFunction implements neutronCallable {

    private final Stmt.Function declaration;
    private final Enviornment closure;

    neutronFunction(Stmt.Function delaration, Enviornment closure) {
        this.declaration = delaration;
        this.closure = closure;
    }

    // Call method to create new enviorment for each function call
    // and executing it's code using interperater execute block method.
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Enviornment enviornment = new Enviornment(this.closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            enviornment.define(declaration.params.get(i), arguments.get(i));
        }
        try {

            interpreter.executeBlock(declaration.body, enviornment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    // Method for return the lenght of parameters.
    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + " >";
    }
}
