package mapl.ast.util;

import mapl.ast.TypeBoolean;
import mapl.ast.ExpCall;
import mapl.ast.ExpVar;
import mapl.ast.ExpOp;
import mapl.ast.StmArrayAssign;
import mapl.ast.Formal;
import mapl.ast.ExpFalse;
import mapl.ast.StmVarDecl;
import mapl.ast.StmIf;
import mapl.ast.ExpArrayLookup;
import mapl.ast.Program;
import mapl.ast.StmCall;
import mapl.ast.StmOutput;
import mapl.ast.StmAssign;
import mapl.ast.ExpInteger;
import mapl.ast.StmBlock;
import mapl.ast.ExpArrayLength;
import mapl.ast.StmOutchar;
import mapl.ast.ProcDecl;
import mapl.ast.ExpIsnull;
import mapl.ast.TypeInt;
import mapl.ast.ExpNewArray;
import mapl.ast.ExpNot;
import mapl.ast.ExpTrue;
import mapl.ast.TypeArray;
import mapl.ast.StmWhile;
import mapl.ast.FunDecl;

public interface Visitor<T> {

    public T visit(Program n);

    public T visit(ProcDecl n);

    public T visit(FunDecl n);

    public T visit(Formal n);

    public T visit(TypeBoolean n);

    public T visit(TypeInt n);

    public T visit(TypeArray n);

    public T visit(StmBlock n);

    public T visit(StmVarDecl n);

    public T visit(StmIf n);

    public T visit(StmWhile n);

    public T visit(StmOutput n);

    public T visit(StmOutchar n);

    public T visit(StmAssign n);

    public T visit(StmArrayAssign n);

    public T visit(StmCall n);

    public T visit(ExpCall n);

    public T visit(ExpInteger n);

    public T visit(ExpTrue n);

    public T visit(ExpFalse n);

    public T visit(ExpVar n);

    public T visit(ExpNot n);
    
    public T visit(ExpOp n);

    public T visit(ExpArrayLookup n);

    public T visit(ExpArrayLength n);

    public T visit(ExpNewArray n);

    public T visit(ExpIsnull n);
}
