package mapl.ast;

import mapl.ast.util.Visitor;

public abstract class Type extends AST {

    public abstract <T> T accept(Visitor<T> v);
}
