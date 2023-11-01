import java.util.List;

public class neutronClass implements neutronCallable {
    final String name;

    neutronClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Class " + this.name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return 1;
    }

    @Override
    public int arity() {
        return 0;
    }
}
