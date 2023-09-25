import java.util.HashMap;
import java.util.Map;

public class Enviornment {

    public final Map<String, Object> values = new HashMap<>();
    final Enviornment enclosing;

    // Constructor which takes an enclosing argument;
    public Enviornment(Enviornment enclosing) {
        this.enclosing = enclosing;
    }

    // Constructor without any enclosig argument;
    public Enviornment() {
        this.enclosing = null;
    }

    void define(Token key, Object value) {
        if (values.containsKey(key.lexeme))
            throw new RuntimeError(key, "Cannot redefine a variable.");
        values.put(key.lexeme, value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        // Calling the scope which contains the current scope to find the variable;
        // This process happens recursively until we reaches the global scope.
        if (enclosing != null)
            return enclosing.get(name);
        throw new RuntimeError(name, "Unkown Indentifier " + name.lexeme + ".");
    }

    void assign(Token key, Object value) {
        if (values.containsKey(key.lexeme)) {
            values.put(key.lexeme, value);
            return;
        }
        /*
         * same thing is happing here, if it didn't find variable
         * it finds recusively in enclosing one, untill it reaches
         * the global scope.
         */
        if (enclosing != null) {
            enclosing.assign(key, value);
            return;
        }
        throw new RuntimeError(key, "variable is not defined.");
    }

}
