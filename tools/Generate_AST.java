/*
 * The whole purpuse of this file is to automate the process of
 * creating classes for Abstract syntax tree.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class Generate_AST {

    /*
     * For statements
     * program → statement* EOF ;
     * 
     * statement → exprStmt
     * | printStmt ;
     * 
     * exprStmt → expression ";" ;
     * printStmt → "print" expression ";" ;
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: Generate_AST <OUTPUT DIRECTORY>");
            System.exit(64);
        }

        String outputDir = args[0];
        System.out.println("outputDir: " + outputDir);

        /*
         * Here class name is followed by ':' and after that there are it
         * constructor arguments
         */
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary: Expr left, Token operator, Expr right",
                "Grouping: Expr experession",
                "Literal: Object value",
                "Unary: Token operator, Expr right",
                "Variable: Token name"));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression: Expr expression",
                "Print: Expr expression",
                "Var: Token name, Expr initializer"));
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types) throws IOException {
        String Path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(Path, "UTF-8");

        writer.println("import java.util.List;");
        writer.println();
        // Creating a class with basename and writing classes in it.
        writer.println("abstract class " + baseName + "{");

        defineVisitor(writer, baseName, types);

        // The base accept() method.
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        // for The ATS classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();

            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    // Method for defining each subclass;

    private static void defineType(
            PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("   static class " + className + " extends " + baseName + " {");
        // constructor
        writer.println("    " + className + "(" + fieldList + "){");
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("    this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }

    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
}
