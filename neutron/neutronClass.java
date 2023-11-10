import java.util.List;
import java.util.Map;

// it's an java repersentation of neutron classes.
public class neutronClass extends neutronInstance implements neutronCallable {
    final String name;
    final Map<String, neutronFunction> methods;
    final Map<String, neutronFunction> staticMethods;

    neutronClass(String name, Map<String, neutronFunction> methods, Map<String, neutronFunction> staticMethods) {
        this.name = name;
        this.methods = methods;
        this.staticMethods = staticMethods;
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
        neutronFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        neutronFunction initializer = findMethod("init");
        if (initializer != null)
            return initializer.arity();
        return 0;
    }

    neutronFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }

    @Override
    public Object get(Token name) {
        if (staticMethods.containsKey(name.lexeme)) {
            return staticMethods.get(name.lexeme);
        }
        throw new RuntimeError(name, "Undefined static method " + name.lexeme + ".");
    }

    public Boolean isStatic(String name) {
        return staticMethods.containsKey(name);
    }
}
