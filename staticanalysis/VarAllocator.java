package mapl.staticanalysis;

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
import mapl.ast.Stm;
import mapl.ast.StmAssign;
import mapl.ast.ExpInteger;
import mapl.ast.MethodDecl;
import mapl.ast.StmBlock;
import mapl.ast.ExpArrayLength;
import mapl.ast.StmOutchar;
import mapl.ast.ProcDecl;
import mapl.ast.Exp;
import mapl.ast.ExpIsnull;
import mapl.ast.ExpNewArray;
import mapl.ast.ExpNot;
import mapl.ast.ExpTrue;
import mapl.ast.Var;
import mapl.ast.StmWhile;
import mapl.ast.FunDecl;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mapl.staticanalysis.SymbolTable;
import mapl.ast.util.VisitorAdapter;

/**
 *
 */
public class VarAllocator extends VisitorAdapter<Void> {
    
    int count, maxCount;
    
    Deque<Map<String, Integer>> blockStack;
    
    SymbolTable symTab;
    
    public VarAllocator(SymbolTable symTab) {
        this.symTab = symTab;
        count = 0;
        maxCount = 0;
        blockStack = new java.util.LinkedList<>();
    }
    
    private void assignOffset(Var v) {
        for (Map<String, Integer> block : blockStack) {
            Integer offset = block.get(v.id);
            if (offset != null) {
                v.offset = offset;
                v.isStackAllocated = true;
                return;
            }
        }
        throw new StaticAnalysisException(v + " is not stack allocated but Mapl only has stack allocated variables!");
    }
    
    private void pushBlock() {
        blockStack.push(new HashMap<>());
    }
    
    private void popBlock() {
        Map<String, Integer> block = blockStack.pop();
        count = count - block.size();
    }
    
    private void allocLocal(String id, int offset) {
        Map<String, Integer> block = blockStack.peek();
        block.put(id, offset);
    }
    
    // ProcDecl pd;
    // List<FunDecl> fds;
    public Void visit(Program n) {
        n.pd.accept(this)
;        for (MethodDecl md : n.mds) {
            md.accept(this);
        }
        return null;
    }
    
    /**
     * Allocate variables for a method.
     * 
     * @param id the method name
     * @param fds the formal parameters of the method declaration
     * @param ss the statements in the body of the declaration
     * @return the number of slots which need to be allocated on the stack for
     * local variables (NOT parameters) when executing a call to this method
     */
    private int allocMethod(String id, List<Formal> fds, List<Stm> ss) {
        count = 0;
        maxCount = 0;
        // allocate parameters
        int arity = fds.size();
        for (int i = 0; i < arity; ++i) {
            Formal f = fds.get(i);
            allocLocal(f.id, i + 2);
        }
        // process method body
        for (Stm s : ss) {
            s.accept(this);
        }
        return maxCount;
    }
    
    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    // int stackAllocation;
    // Type t;
    // Exp e;
    public Void visit(FunDecl n) {
        pushBlock();
        n.stackAllocation = allocMethod(n.id, n.fs, n.ss);
        n.e.accept(this);
        popBlock(); // leaving method scope
        return null;
    }

    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    // int stackAllocation;
    public Void visit(ProcDecl n) {
        pushBlock();
        n.stackAllocation = allocMethod(n.id, n.fs, n.ss);
        popBlock(); // leaving method scope
        return null;
    }

    // List<Stm> ss;
    public Void visit(StmBlock n) {
        pushBlock();
        for (Stm s : n.ss) {
            s.accept(this);
        }
        popBlock();
        return null;
    }

    // Type t;
    // String id;
    public Void visit(StmVarDecl n) {
        ++count;
        allocLocal(n.id, -count);
        maxCount = Math.max(count, maxCount);
        return null;
    }

    // Exp e;
    // Stm st, sf;
    public Void visit(StmIf n) {
        n.e.accept(this);
        n.st.accept(this);
        n.sf.accept(this);
        return null;
    }

    // Exp e;
    // Stm body;
    public Void visit(StmWhile n) {
        n.e.accept(this);
        n.body.accept(this);
        return null;
    }

    // Exp e;
    public Void visit(StmOutput n) {
        n.e.accept(this);
        return null;
    }

    // Exp e;
    public Void visit(StmOutchar n) {
        n.e.accept(this);
        return null;
    }

    // Var v;
    // Exp e;
    public Void visit(StmAssign n) {
        assignOffset(n.v);
        n.e.accept(this);
        return null;
    }

    // Exp e1,e2,e3;
    public Void visit(StmArrayAssign n) {
        n.e1.accept(this);
        n.e2.accept(this);
        n.e3.accept(this);
        return null;
    }

    // String id;
    // List<Exp> es;
    public Void visit(StmCall n) {
        for (Exp e : n.es) {
            e.accept(this);
        }
        return null;
    }

    // String id;
    // List<Exp> es;
    public Void visit(ExpCall n) {
        for (Exp e : n.es) {
            e.accept(this);
        }
        return null;
    }

    // int i;
    public Void visit(ExpInteger n) {
        return null;
    }

    public Void visit(ExpTrue n) {
        return null;
    }

    public Void visit(ExpFalse n) {
        return null;
    }

    // Var v;
    public Void visit(ExpVar n) {
        assignOffset(n.v);
        return null;
    }

    // Exp e;
    public Void visit(ExpNot n) {
        n.e.accept(this);
        return null;
    }
    
    // Exp e1, e2;
    // ExpOp.Op op;
    public Void visit(ExpOp n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e1,e2;
    public Void visit(ExpArrayLookup n) {
        n.e1.accept(this);
        n.e2.accept(this);
        return null;
    }

    // Exp e;
    public Void visit(ExpArrayLength n) {
        n.e.accept(this);
        return null;
    }

    // Type t;
    // Exp e;
    public Void visit(ExpNewArray n) {
        n.e.accept(this);
        return null;
    }

    // Exp e;
    public Void visit(ExpIsnull n) {
        n.e.accept(this);
        return null;
    }

}
