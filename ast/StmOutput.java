package mapl.ast;

import mapl.ast.util.Visitor;

public class StmOutput extends Stm {

    public final Exp e;

    public StmOutput(Exp ae) {
        e = ae;
    }

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
