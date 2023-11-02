import java.util.HashMap;
import java.util.Map;

public class neutronInstance {
    private neutronClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    neutronInstance(neutronClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance.";
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        // If instance doesn't have property with name.
        throw new RuntimeError(name, "Undefined property " + name.lexeme + ".");
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
