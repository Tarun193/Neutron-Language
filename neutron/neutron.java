
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class neutron {
    private static final Interpreter interpreter = new Interpreter();

    // Flag to stop, execution if program has any error.
    static boolean hadError = false;
    // Flag for runtime errors
    static boolean hadRuntimeError = false;

    public static void main(String args[]) throws IOException {
        // if user give more that one file name or add space in file name
        if (args.length > 1) {
            System.out.println("usage: neutron [script].neutron");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // Indicate an error in the exit code.
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        /*
         * Input stream reader is a bridge between byte stream and character stream, It
         * reads bytes and decodes them into characters using a specified charset.
         * BufferedReader reads lines from the file in chunks, making the reading
         * process more efficient.
         */
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print(">>> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            // Reseting the had error so that interactive mode does not get killed
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError)
            return;

        interpreter.interpreter(expression);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void report(int line, String where, String message) {
        System.out.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "/n [line " + error.token.line + " ]");
        hadRuntimeError = true;
    }
}