import java.util.List;
import java.util.Map;

// it's an java repersentation of neutron classes.
public class neutronClass implements neutronCallable {
    final String name;
    final Map<String, neutronFunction> methods;

    neutronClass(String name, Map<String, neutronFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "Class " + this.name;
    }

    // When a class is called it will create an instance of that class using,
    // neutron instance class.
    // neutron instance class is java repersentation of neutron instances.
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        neutronInstance instance = new neutronInstance(this);
        return instance;
    }

    @Override
    public int arity() {
        return 0;
    }

    neutronFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }
}
