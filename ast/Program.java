package mapl.ast;

import java.util.List;
import mapl.ast.util.Visitor;

public class Program extends AST {

    public final ProcDecl pd;
    public final List<MethodDecl> mds;

    public Program(ProcDecl pd, List<MethodDecl> mds) {
        this.pd = pd;
        this.mds = mds;
    }

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
