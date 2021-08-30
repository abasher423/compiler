package mapl.ast;

import mapl.ast.util.Visitor;

public class ExpTrue extends Exp {

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
