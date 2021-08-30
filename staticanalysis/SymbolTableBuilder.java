package mapl.staticanalysis;

import mapl.ast.Program;
import mapl.ast.ProcDecl;
import mapl.ast.Formal;
import mapl.ast.Type;
import mapl.ast.FunDecl;
import mapl.ast.MethodDecl;
import java.util.ArrayList;
import java.util.List;
import mapl.ast.util.VisitorAdapter;

/**
 * Visitors which build a symbol table for a Mapl AST.
 */
public class SymbolTableBuilder extends VisitorAdapter<Void> {

    private SymbolTable symbolTable;

    /**
     * Initialise a new symbol table builder.
     */
    public SymbolTableBuilder() {
        symbolTable = new SymbolTable();
    }

    /**
     * The symbol table which has been built so far.
     * @return the symbol table
     */
    public SymbolTable getSymTab() {
        return symbolTable;
    }

    // ProcDecl pd;
    // List<MethodDecl> mds;
    public Void visit(Program n) {
        n.pd.accept(this);
        for (MethodDecl md : n.mds) {
            md.accept(this);
        }
        return null;
    }

    // Type t;
    // String id;
    // List<Formal> fds;
    // List<Stm> ss;
    // Exp e;
    @Override
    public Void visit(FunDecl n) {
        String methodName = n.id;
        List<Type> formalTypes = new ArrayList<Type>();
        for (Formal f : n.fs) {
            formalTypes.add(f.t);
        }
        if (!symbolTable.addMethod(methodName, n.t, formalTypes)) {
            throw new StaticAnalysisException("Method " + methodName
                    + " is already defined", n.getTags());
        }
        return null;
    }

    // String id;
    // List<Formal> fds;
    // List<Stm> ss;
    @Override
    public Void visit(ProcDecl n) {
        String methodName = n.id;
        List<Type> formalTypes = new ArrayList<Type>();
        for (Formal f : n.fs) {
            formalTypes.add(f.t);
        }
        if (!symbolTable.addMethod(methodName, null, formalTypes)) {
            throw new StaticAnalysisException("Method " + methodName
                    + " is already defined", n.getTags());
        }
        return null;
    }
}