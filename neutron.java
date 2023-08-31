
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

class neutron {

    // Flag to stop, execution if program has any error.
    static boolean hadError = false;

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
        // TODO: Token and Scanner class needs Implementation.
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void report(int line, String where, String message) {
        System.out.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}