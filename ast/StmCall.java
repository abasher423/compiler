package mapl.ast;

import java.util.List;
import mapl.ast.util.Visitor;

public class StmCall extends Stm {

    public final String id;
    public final List<Exp> es;

    public StmCall(String aid, List<Exp> aes) {
        id = aid;
        es = aes;
    }

    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }
}
