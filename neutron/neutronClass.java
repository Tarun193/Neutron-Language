import java.util.List;

// it's an java repersentation of neutron classes.
public class neutronClass implements neutronCallable {
    final String name;

    neutronClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Class " + this.name;
    }

    // When a class is called it will create an instance of that class using, neutron instance class.
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
}
