package mapl.ast;

import mapl.ast.util.Visitor;

public class StmIf extends Stm {

    public final Exp e;
    public final Stm st, sf;

    public StmIf(Exp e, Stm st, Stm sf) {
        this.e = e;
        this.st = st;
        this.sf = sf;
    }

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
