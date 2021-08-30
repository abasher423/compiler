package mapl.ast;

import mapl.ast.util.Visitor;

public class ExpFalse extends Exp {

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
