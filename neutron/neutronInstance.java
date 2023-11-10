import java.util.HashMap;
import java.util.Map;

public class neutronInstance {
    private neutronClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    neutronInstance() {
    }

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

        if (klass.isStatic(name.lexeme))
            throw new RuntimeError(name, "Can not call a static method an instance");
        // If the given token is not an state, it can be method then
        neutronFunction method = klass.findMethod(name.lexeme);

        if (method != null)
            return method.bind(this);
        // If instance doesn't have property with name.
        throw new RuntimeError(name, "Undefined property " + name.lexeme + ".");
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
