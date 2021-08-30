package mapl.ast;

import java.util.List;
import mapl.ast.util.Visitor;

public class StmBlock extends Stm {

    public final List<Stm> ss;

    public StmBlock(List<Stm> ass) {
        ss = ass;
    }

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
