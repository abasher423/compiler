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

/** Implements Visitor with trivial methods (all throw an error). */
public class VisitorAdapter<T> implements Visitor<T>  {

    // ProcDecl pd;
    // List<MethodDecl> mds;
    public T visit(Program n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Type t;
    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    // Exp e;
    public T visit(FunDecl n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    public T visit(ProcDecl n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Type t;
    // String id;
    public T visit(Formal n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    public T visit(TypeBoolean n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    public T visit(TypeInt n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    public T visit(TypeArray n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // List<Stm> ss;
    public T visit(StmBlock n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Type t;
    // String id;
    public T visit(StmVarDecl n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e;
    // Stm st, sf;
    public T visit(StmIf n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e;
    // Stm body;
    public T visit(StmWhile n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e;
    public T visit(StmOutput n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e;
    public T visit(StmOutchar n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Var v;
    // Exp e;
    public T visit(StmAssign n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e1,e2,e3;
    public T visit(StmArrayAssign n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // String id;
    // List<Exp> es;
    public T visit(StmCall n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // String id;
    // List<Exp> es;
    public T visit(ExpCall n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // int i;
    public T visit(ExpInteger n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    public T visit(ExpTrue n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    public T visit(ExpFalse n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Var v;
    public T visit(ExpVar n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e;
    public T visit(ExpNot n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }
    
    // Exp e1, e2;
    // ExpOp.Op op;
    public T visit(ExpOp n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e1,e2;
    public T visit(ExpArrayLookup n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e;
    public T visit(ExpArrayLength n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Type t;
    // Exp e;
    public T visit(ExpNewArray n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }

    // Exp e;
    public T visit(ExpIsnull n) {
        throw new Error("visitor called on unexpected AT node type: " + n);
    }
}

