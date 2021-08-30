package mapl.ast;

import java.util.List;
import mapl.ast.util.Visitor;

public class ProcDecl extends MethodDecl {

    public ProcDecl(String id, List<Formal> fs,  List<Stm> ss) {
        super(id, fs, ss);
    }

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
