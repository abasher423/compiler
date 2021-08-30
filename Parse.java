package mapl;

import mapl.parser.MaplParser;

/**
 * A harness to test the Mapl parser.
 */
public class Parse {

    public static void main(String[] args) throws Throwable {
        MaplParser parser;
        try {
            try {
                parser = new MaplParser(new java.io.FileInputStream(args[0]));
            } catch (java.io.FileNotFoundException e) {
                System.err.println("Unable to read file " + args[0]);
                return;
            }
            System.out.println("parsing...");
            parser.nt_Program();
            System.out.println("...parse completed.");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
