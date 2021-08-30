package mapl.ast;

import mapl.ast.util.Visitor;

public abstract class Exp extends AST {

    public abstract <T> T accept(Visitor<T> v);
}
