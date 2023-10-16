import java.util.List;

public class neutronFunction implements neutronCallable {

    private final Stmt.Function declaration;

    neutronFunction(Stmt.Function delaration) {
        this.declaration = delaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Enviornment enviornment = new Enviornment(interpreter.global);
        for (int i = 0; i < declaration.params.size(); i++) {
            enviornment.define(declaration.params.get(i), arguments.get(i));
        }
        interpreter.executeBlock(declaration.body, enviornment);
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + " >";
    }
}
