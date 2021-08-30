package mapl;

import mapl.parser.MaplParser;
import mapl.ast.util.PrettyPrinter;

/**
 * A harness to test that the abstract syntax tree is being built correctly. The
 * main method pretty-prints the AST to standard out.
 */
public class PrettyPrint {

    public static void main(String[] args) {
        MaplParser parser;
        try {
            try {
                parser = new MaplParser(new java.io.FileInputStream(args[0]));
            } catch (java.io.FileNotFoundException e) {
                System.err.println("Unable to read file " + args[0]);
                return;
            }
            parser.nt_Program().accept(new PrettyPrinter());
        } catch (Throwable e) {
            //System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
