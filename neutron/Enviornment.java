import java.util.HashMap;
import java.util.Map;

public class Enviornment {

    public final Map<String, Object> values = new HashMap<>();

    void define(Token key, Object value) {
        if (values.containsKey(key.lexeme))
            throw new RuntimeError(key, "Cannot redefine a variable.");
        values.put(key.lexeme, value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        throw new RuntimeError(name, "Unkown Indentifier " + name.lexeme + ".");
    }

}
