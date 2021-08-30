package mapl.ast;

import mapl.ast.util.Visitor;

public class StmWhile extends Stm {

    public final Exp e;
    public final Stm body;

    public StmWhile(Exp e, Stm body) {
        this.e = e;
        this.body = body;
    }

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
