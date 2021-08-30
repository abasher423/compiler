package mapl;


import mapl.ast.Program;
import mapl.staticanalysis.UseDefChecker;
import mapl.staticanalysis.TypeChecker;
import mapl.staticanalysis.SymbolTable;
import mapl.staticanalysis.StaticAnalysisException;
import mapl.staticanalysis.SymbolTableBuilder;
import cloptions.CLOptions;
import mapl.parser.TokenMgrError;
import mapl.parser.ParseException;
import mapl.parser.MaplParser;
import static cloptions.CLOptions.basePath;
import mapl.ast.util.PrettyPrinter;
import mapl.ast.util.Visitor;
import mapl.compiler.Compiler;
import ir.ast.util.IRPrettyPrinter;
import ir.ast.IRProgram;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * A harness to run the compiler.
 */
public class Compile {

    private static boolean
            source = false,
            usedef = true,
            type = true,
            quiet = false;

    private Compile() {
    }

    /**
     * Compile Mapl source code to IR code.
     * <p>
     * Command line arguments: Mapl source file name
     * <p>
     * Options: <ul>
     * <li> -notype (disable type checking)
     * <li> -source (pretty-print parsed input)
     * <li> -quiet (suppress progress messages)
     * <li> -nousedef (disable check for uninitialised local variables)
     * </ul>
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        Set<String> options = CLOptions.options(argList, "notype", "source", "quiet", "nousedef").keySet();
        type = !options.contains("notype");
        source = options.contains("source");
        quiet = options.contains("quiet");
        usedef = !options.contains("nousedef");
        Program root;
        try {
            String inputFileName = argList.get(0);
            report("Parsing...");
            System.out.flush();
            // Read program to be parsed from file
            try {
                root = new MaplParser(new java.io.FileInputStream(inputFileName)).nt_Program();
            } catch (java.io.FileNotFoundException e) {
                System.err.println("Unable to read file " + inputFileName);
                return;
            }
            reportln("...parsed OK.");
            SymbolTable symTab;
            {
                SymbolTableBuilder stvisit = new SymbolTableBuilder();
                report("Building Symbol Table...");
                System.out.flush();
                root.accept(stvisit);
                reportln("...done");
                symTab = stvisit.getSymTab();
            }
            if (type) {
                TypeChecker typeChecker = new TypeChecker(symTab);
                report("Type checking...");
                System.out.flush();
                root.accept(typeChecker);
                reportln("...type checked OK.");
            }
            if (usedef) {
                report("Checking local variable def-before-use...");
                UseDefChecker udChecker = new UseDefChecker();
                root.accept(udChecker);
                reportln("...OK.");
            }
            {
                report("Allocating variables...");
                System.out.flush();
                Visitor<Void> varAllocator = new mapl.staticanalysis.VarAllocator(symTab);
                root.accept(varAllocator);
                reportln("...done.");
            }
            if (source) {
                root.accept(new PrettyPrinter());
            }
            Compiler compiler = new mapl.compiler.Compiler();
            
            report("Compiling...");
            IRProgram p = compiler.compile(root);
            String outputFileName = "out";
            if (!"-".equals(inputFileName)) {
                outputFileName = basePath(inputFileName);
            }
            File irFile = new File(outputFileName + ".ir");
            reportln("Writing IR code to " + irFile.getPath());
            output(irFile, p);
        } catch (ParseException | TokenMgrError e) {
            System.out.println("\nSyntax error: " + e.getMessage());
        } catch (StaticAnalysisException e) {
            System.out.println("\nStatic semantics error: " + e.getMessage());
        }
    }

    private static void output(File f, IRProgram p) {
        try (PrintStream out = new PrintStream(new FileOutputStream(f))) {
            IRPrettyPrinter.prettyPrint(out, p);
        } catch (IOException e) {
            System.err.println(e);
            throw new Error("Errror writing to file: " + f.getName());
        }
    }

    private static void report(String msg) {
        if (!quiet) {
            System.out.print(msg);
            System.out.flush();
        }
    }

    private static void reportln(String msg) {
        report(msg + "\n");
    }
}
