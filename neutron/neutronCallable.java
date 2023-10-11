import java.util.List;

// all the lox objects that can be called, will implement this interface.
interface neutronCallable {
    // returns the number of argument required;
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
