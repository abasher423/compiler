package mapl.compiler;

import mapl.ast.*;
import mapl.ast.util.VisitorAdapter;
import ir.ast.*;
import mapl.staticanalysis.VarAllocator;

import static mapl.compiler.FreshNameGenerator.makeName;

import java.util.*;

public class Compiler {


    private final StmCompiler stmCompiler;
    private final ExpCompiler expCompiler;

    public Compiler() {
        stmCompiler = new StmCompiler();
        expCompiler = new ExpCompiler();
    }

    /**************************************************************************/
    /*                                                                        */
    /* The following factory methods methods are not strictly necessary but   */
    /* they greatly simplify the code that you have to write. For example,    */
    /* instead of:                                                            */
    /*                                                                        */
    /*    new IRStmMoveTemp("x",                                              */
    /*                      new IRExpBinOp(new IRExpTemp("y"),                */
    /*                                     IROp.EQ,                           */
    /*                                     new IRExpConst(7)                  */
    /*                                    )                                   */
    /*                     )                                                  */
    /*                                                                        */
    /* you can write:                                                         */
    /*                                                                        */
    /*    MOVE(TEMP("x"), BINOP(TEMP("y"), IROp.EQ, CONST(7)))                */
    /*                                                                        */
    /**************************************************************************/

    /****************************************************/
    /* Convenience factory methods for building IRStms. */
    /****************************************************/

    private static IRStm MOVE(IRExp el, IRExp er) {
        if (el instanceof IRExpTemp) {
            return new IRStmMoveTemp(((IRExpTemp)el).name, er);
        } else if (el instanceof IRExpMem) {
            return new IRStmMoveMem(((IRExpMem)el).e, er);
        } else {
            throw new Error("Left-expression of MOVE must be either a TEMP or a MEM, not: " + el);
        }
    }

    private static IRStmNoop NOOP = new IRStmNoop();

    private static IRStmJump JUMP(IRExp e) {
        return new IRStmJump(e);
    }

    private static IRStmCJump CJUMP(IRExp e1, IROp op, IRExp e2, String trueLabel, String falseLabel) {
        return new IRStmCJump(e1 , op, e2, trueLabel, falseLabel);
    }

    private static IRStmExp EXP(IRExp e) {
        return new IRStmExp(e);
    }

    private static IRStmLabel LABEL(String name) {
        return new IRStmLabel(name);
    }

    private static IRStm SEQ(IRStm... stms) {
        int n = stms.length;
        if (n == 0) return NOOP;
        IRStm stm = stms[n-1];
        for (int i = n-2; i >= 0; --i) {
            stm = new IRStmSeq(stms[i], stm);
        }
        return stm;
    }

    private static IRStm SEQ(List<IRStm> stms) {
        return SEQ(stms.toArray(new IRStm[0]));
    }

    private static IRStmPrologue PROLOGUE(int params, int locals) {
        return new IRStmPrologue(params, locals);
    }

    private static IRStmEpilogue EPILOGUE(int params, int locals) {
        return new IRStmEpilogue(params, locals);
    }

    /****************************************************/
    /* Convenience factory methods for building IRExps. */
    /****************************************************/

    private static IRExpBinOp BINOP(IRExp e1, IROp op, IRExp e2) {
        return new IRExpBinOp(e1, op, e2);
    }

    private static IRExpCall CALL(IRExp f, IRExp... args) {
        return new IRExpCall(f, args);
    }

    private static IRExpCall CALL(IRExp f, List<IRExp> args) {
        return new IRExpCall(f, args);
    }

    private static IRExpConst CONST(int n) {
        return new IRExpConst(n);
    }

    private static IRExpMem MEM(IRExp e) {
        return new IRExpMem(e);
    }

    private static IRExpTemp TEMP(String name) {
        return new IRExpTemp(name);
    }

    private static IRExpName NAME(String labelName) {
        return new IRExpName(labelName);
    }

    private static IRExpESeq ESEQ(IRStm s, IRExp e) {
        return new IRExpESeq(s, e);
    }

    // TODO: extend this to handle more complex programs
    // The initial prototype assumes just a single top-level proc with zero
    // parameters. The extended version will retrieve the command-line
    // parameters from the stack and then CALL the top-level proc.
    public IRProgram compile(Program n) {
        List<IRStm> stms = new ArrayList<>();
        List<IRExp> arguments = new ArrayList<>();
        for (int i = 0; i <n.pd.fs.size() ; i++) {
            IRExp exp = MEM(BINOP(TEMP("FP"),IROp.SUB,CONST(i+1)));
            arguments.add(exp);
        }
        IRExp exp2 = CALL(NAME(n.pd.id),arguments);
        stms.add(EXP(exp2));
        stms.add(JUMP(NAME("_END")));

        stms.addAll(n.pd.accept(stmCompiler));

        for (int i = 0; i <n.mds.size() ; i++) {
            stms.addAll(n.mds.get(i).accept(stmCompiler));
        }



        Map<String,String> Defs = new HashMap<>();

        Defs.put("InputIsNegative ","the index is below 0");
        Defs.put("ArrayIsOutOfBounds ","index outside the array");
        return new IRProgram(Defs,stms);
    }

    // TODO: add visit methods for all the Stm classes
    // TODO: add visit methods for method declarations
    // Note: no need to define visit methods for any other AST types
    private class StmCompiler extends VisitorAdapter<List<IRStm>> {


        @Override
        public List<IRStm> visit(StmOutchar s) {
            List<IRStm> stms = new ArrayList<>();
            IRExp exp = s.e.accept(expCompiler);
            stms.add(EXP(CALL(NAME("_printchar"), exp)));
            return stms;
        }

        @Override
        public List<IRStm> visit(StmOutput s){
            List<IRStm> stms = new ArrayList<>();
            IRExp exp = s.e.accept(expCompiler);
            stms.add(EXP(CALL(NAME("_printint"),exp)));
            return stms;
        }

        @Override
        public List<IRStm> visit(StmIf s){
            List<IRStm> stms = new ArrayList<>();
            List<IRStm> exp2 = s.sf.accept(stmCompiler);

            List<IRStm> exp = s.st.accept(stmCompiler);

            IRExp e1 = s.e.accept(expCompiler);


            String s1 = FreshNameGenerator.makeName("if");
            String s2 = FreshNameGenerator.makeName("if");

            String s3 = FreshNameGenerator.makeName("if");

            stms.add(CJUMP(e1, IROp.EQ, CONST(1), s1, s2));
            stms.add(LABEL(s1));
            stms.addAll(exp);
            stms.add(JUMP(NAME(s3)));
            stms.add(LABEL(s2));
            stms.addAll(exp2);
            stms.add(LABEL(s3));

            return stms;

        }
        @Override
        public List<IRStm> visit(StmWhile s){
            List<IRStm> stms = new ArrayList<>();
            List<IRStm> statements = s.body.accept(stmCompiler);
            IRExp e1 = s.e.accept(expCompiler);



            String jump = FreshNameGenerator.makeName("jump");
            String true1 = FreshNameGenerator.makeName("true");
            String false1 = FreshNameGenerator.makeName("false");

            stms.add(LABEL(jump));
            stms.add(CJUMP(e1, IROp.EQ, CONST(1), true1, false1));
            stms.add(LABEL(true1));
            stms.addAll(statements);
            stms.add(JUMP(NAME(jump)));
            stms.add(LABEL(false1));

            return stms;
        }

        @Override
        public List<IRStm> visit(StmBlock n) {
            List<IRStm> stms = new ArrayList<>();
            for (Stm stm : n.ss){
                stms.addAll(stm.accept(stmCompiler));
            }
            return stms;
        }

        @Override
        public List<IRStm> visit(StmAssign s){
            IRExp e1 = s.e.accept(expCompiler);
            List<IRStm> stms = new ArrayList<>();
            stms.add(MOVE(MEM(BINOP(TEMP("FP"),IROp.ADD,CONST(s.v.offset))),e1));
            return stms;
        }
        @Override
        public List<IRStm> visit(StmVarDecl n) {
            return new ArrayList<>();
        }
        @Override
        public List<IRStm> visit(StmCall s){
            List<IRStm> stms = new ArrayList<>();
            List<IRExp> exps = new ArrayList<>();
            for (Exp exp : s.es){
                exps.add(exp.accept(expCompiler));
            }

            stms.add(EXP(CALL(NAME(s.id),exps)));

            return stms;
        }
        @Override
        public List<IRStm> visit(ProcDecl n){
            List<IRStm> stms = new ArrayList<>();
            List<IRStm> irs1 = new ArrayList<>();
            for (Stm stm: n.ss){
                irs1.addAll(stm.accept(stmCompiler));
            }
            stms.add(LABEL((n.id)));
            stms.add(PROLOGUE(n.fs.size(),n.stackAllocation));
            stms.addAll(irs1);
            stms.add(EPILOGUE(n.fs.size(),n.stackAllocation));
            return stms;
        }

        @Override
        public List<IRStm> visit(FunDecl n) {
            List<IRStm> stms = new ArrayList<>();
            List<IRStm> irs1 = new ArrayList<>();
            for (Stm stm: n.ss){
                irs1.addAll(stm.accept(stmCompiler));
            }
            int size = n.fs.size();
            IRExp exp = n.e.accept(expCompiler);
            stms.add(LABEL((n.id)));
            stms.add(PROLOGUE(size,n.stackAllocation));
            stms.addAll(irs1);
            stms.add(MOVE(TEMP("RV"),exp));
            stms.add(EPILOGUE(size,n.stackAllocation));
            return stms;
        }

        @Override
        public List<IRStm> visit(StmArrayAssign n) {
            List<IRStm> stms = new ArrayList<>();
            IRExp exp1 = n.e1.accept(expCompiler);
            IRExp exp2 = n.e2.accept(expCompiler);
            IRExp exp3 = n.e3.accept(expCompiler);
            String true1 = FreshNameGenerator.makeName("t1");
            String false1 = FreshNameGenerator.makeName("f1");

            String true2 = FreshNameGenerator.makeName("t2");
            String false2 = FreshNameGenerator.makeName("f2");
            stms.add(CJUMP(BINOP(exp2,IROp.LT,CONST(0)),
                    IROp.EQ,
                    CONST(1),
                    true1,
                    false1
            ));
            stms.add(LABEL(true1));
            stms.add(EXP(CALL(NAME("_printstr"),NAME("InputIsNegative"))));
            stms.add(JUMP(NAME("_END")));
            stms.add(LABEL(false1));
            stms.add(CJUMP(BINOP(MEM(exp1),IROp.LE,exp2),
                    IROp.EQ,
                    CONST(1),
                    true2,
                    false2
            ));
            stms.add(LABEL(true2));
            stms.add(EXP(CALL(NAME("_printstr"),NAME("ArrayIsOutOfBounds"))));
            stms.add(JUMP(NAME("_END")));
            stms.add(LABEL(false2));
            stms.add(MOVE(MEM(BINOP(exp1, IROp.ADD, BINOP(exp2,IROp.ADD,CONST(1)))),exp3));
            return stms;
        }
    }

    // TODO: add visit methods for all the Exp classes
    // Note: no need to define visit methods for any other AST types
    private class ExpCompiler extends VisitorAdapter<IRExp> {

        @Override
        public IRExp visit(ExpInteger e) {
            return CONST(e.i);
        }

        @Override
        public IRExp visit(ExpVar e) {
            return MEM(BINOP(TEMP("FP"),IROp.ADD,CONST(e.v.offset)));
        }
        @Override
        public IRExp visit(ExpTrue e) {
            return CONST(1);
        }

        @Override
        public IRExp visit(ExpFalse e) {
            return CONST(0);
        }

        @Override
        public IRExp visit(ExpNot e){
            return BINOP(e.e.accept(expCompiler),IROp.LT,CONST(1));

        }

        @Override
        public IRExp visit(ExpOp e) {
            List<IRStm> stms = new ArrayList<>();
            IRExp exp1 = e.e1.accept(expCompiler);
            IRExp exp2 = e.e2.accept(expCompiler);
            if(e.op == ExpOp.Op.AND) {
                String sTrue = FreshNameGenerator.makeName("t_");
                String sFalse = FreshNameGenerator.makeName("f_");
                String sEnd = FreshNameGenerator.makeName("end_");
                String bool = FreshNameGenerator.makeName("bool_");

                stms.add(CJUMP(e.e1.accept(expCompiler),
                        IROp.EQ,
                        CONST(1),
                        sTrue,
                        sFalse));

                stms.add(LABEL(sTrue));
                stms.add(MOVE(TEMP(bool),BINOP(e.e2.accept(expCompiler),IROp.EQ,CONST(1))));
                stms.add(JUMP(NAME(sEnd)));

                stms.add(LABEL(sFalse));
                stms.add(MOVE(TEMP(bool),BINOP(CONST(1),IROp.EQ,CONST(0))));
                stms.add(LABEL(sEnd));
                return ESEQ(SEQ(stms),TEMP(bool));
            }
            return BINOP(exp1,convertOpToIROp(e.op),exp2);

        }

        @Override
        public IRExp visit(ExpCall n) {
            List<IRExp> irExps = new ArrayList<>();
            for (int i = 0; i <n.es.size() ; i++) {
                irExps.add(n.es.get(i).accept(expCompiler));
            }
            return CALL(NAME(n.id),irExps);
        }

        @Override
        public IRExp visit(ExpArrayLength n) {
            IRExp exp = n.e.accept(expCompiler);
            return MEM(exp);
        }

        @Override
        public IRExp visit(ExpArrayLookup n) {
            List<IRStm> stms = new ArrayList<>();
            IRExp exp1 = n.e1.accept(expCompiler);
            IRExp exp2 = n.e2.accept(expCompiler);
            String s1 = FreshNameGenerator.makeName("true1");
            String s2 = FreshNameGenerator.makeName("false1");
            String s3 = FreshNameGenerator.makeName("true");
            String s4 = FreshNameGenerator.makeName("false");
            stms.add(CJUMP(BINOP(exp1,IROp.LT,CONST(0)),
                    IROp.EQ,
                    CONST(1),
                    s1,
                    s2
            ));
            stms.add(LABEL(s1));
            stms.add(EXP(CALL(NAME("_printstr"),NAME("InputIsNegative"))));
            stms.add(JUMP(NAME("_END")));
            stms.add(LABEL(s2));
            stms.add(CJUMP(BINOP(MEM(exp1),IROp.LE,exp2),
                    IROp.EQ,
                    CONST(1),
                    s3,
                    s4
            ));
            stms.add(LABEL(s3));
            stms.add(EXP(CALL(NAME("_printstr"),NAME("ArrayIsOutOfBounds"))));
            stms.add(JUMP(NAME("_END")));
            stms.add(LABEL(s4));

            return ESEQ(SEQ(stms),MEM(BINOP(exp1,IROp.ADD,BINOP(exp2,IROp.ADD,CONST(1)))));
        }
        @Override
        public IRExp visit(ExpNewArray n) {
            IRExp exp = n.e.accept(expCompiler);
            String startOfArray = FreshNameGenerator.makeName("startOfArray_");
            String SizeOfArray = FreshNameGenerator.makeName("SizeOfArray_");
            List<IRStm> stms = new ArrayList<>();

            String true1 = FreshNameGenerator.makeName("t1");
            String false1 = FreshNameGenerator.makeName("f1");
            stms.add(CJUMP(BINOP(exp,IROp.LT,CONST(0)),
                    IROp.EQ,
                    CONST(1),
                    true1,
                    false1));
            stms.add(LABEL(true1));
            stms.add(EXP(CALL(NAME("_printstr"),NAME("InputIsNegative"))));
            stms.add(JUMP(NAME("_END")));

            IRExp expCall = CALL(NAME("_malloc"),BINOP(exp,IROp.ADD,CONST(1)));

            IRStm stmMove = MOVE(TEMP(SizeOfArray),exp);
            IRStm stmMove2 = MOVE(TEMP(startOfArray),expCall);
            IRStm stmMove3 = MOVE(MEM(TEMP(startOfArray)),TEMP(SizeOfArray));

            stms.add(LABEL(false1));
            stms.add(stmMove);
            stms.add(stmMove2);
            String s1 = FreshNameGenerator.makeName("while_");
            String true2 = FreshNameGenerator.makeName("while_");
            String false2 = FreshNameGenerator.makeName("while_");


            String sIndex = FreshNameGenerator.makeName("index_");
            stms.add(MOVE(TEMP(sIndex),CONST(0)));

            stms.add(LABEL(s1));
            stms.add(CJUMP(BINOP(TEMP(sIndex),IROp.LT,exp),
                    IROp.EQ,
                    CONST(1),
                    true2,
                    false2));
            stms.add(LABEL(true2));
            stms.add(MOVE(MEM(BINOP(TEMP(startOfArray),IROp.ADD,BINOP(TEMP(sIndex),IROp.ADD,CONST(1)))),CONST(0)));
            stms.add(MOVE(TEMP(sIndex),BINOP(TEMP(sIndex),IROp.ADD,CONST(1))));

            stms.add(JUMP(NAME(s1)));
            stms.add(LABEL(false2));


            stms.add(stmMove3);
            return ESEQ(SEQ(stms),TEMP(startOfArray));
        }

        @Override
        public IRExp visit(ExpIsnull n){
            return BINOP(n.e.accept(expCompiler),IROp.EQ,CONST(0));
        }

    }
    private IROp convertOpToIROp(ExpOp.Op op){
        if(op == ExpOp.Op.PLUS) return IROp.ADD;
        if(op == ExpOp.Op.MINUS) return IROp.SUB;
        if(op == ExpOp.Op.DIV) return IROp.DIV;
        if(op == ExpOp.Op.TIMES) return IROp.MUL;
        if(op == ExpOp.Op.EQUALS) return IROp.EQ;
        if(op == ExpOp.Op.LESSTHAN) return IROp.LT;
        return null;
    }


}
