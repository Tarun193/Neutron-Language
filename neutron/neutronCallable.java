import java.util.List;

interface neutronCallable {
    Object call(Interpreter interpreter, List<Object> arguments);
}
