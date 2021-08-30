package mapl.staticanalysis;

import mapl.ast.ExpCall;
import mapl.ast.ExpVar;
import mapl.ast.StmArrayAssign;
import mapl.ast.ExpOp;
import mapl.ast.Formal;
import mapl.ast.StmVarDecl;
import mapl.ast.ExpFalse;
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
import mapl.ast.StmOutchar;
import mapl.ast.ExpArrayLength;
import mapl.ast.ProcDecl;
import mapl.ast.Exp;
import mapl.ast.ExpIsnull;
import mapl.ast.ExpNot;
import mapl.ast.ExpNewArray;
import mapl.ast.ExpTrue;
import mapl.ast.StmWhile;
import mapl.ast.FunDecl;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mapl.ast.util.Visitor;
import mapl.ast.util.VisitorAdapter;

/**
 * Check local variables for no-use-before-def violations. This check must be
 * performed <b>after</b> type checking.
 */
public class UseDefChecker extends VisitorAdapter<Set<String>> {

    private static final Integer UNKNOWN = null;
    private static final Integer TRUE = 1;
    private static final Integer FALSE = 0;

    private final Visitor<Integer> staticEval;

    private Deque<Map<String, Boolean>> defStack;

    private boolean blackHole;

    public UseDefChecker() {
        defStack = new java.util.LinkedList<>();
        blackHole = false;
        staticEval = new VisitorAdapter<Integer>() {

            // String id;
            // List<Exp> es;
            public Integer visit(ExpCall n) {
                for (Exp e : n.es) {
                    e.accept(staticEval);
                }
                return null;
            }

            // Exp e1,e2;
            public Integer visit(ExpArrayLookup n) {
                n.e1.accept(staticEval);
                n.e2.accept(staticEval);
                return null;
            }

            // Exp e;
            public Integer visit(ExpArrayLength n) {
                n.e.accept(staticEval);
                return null;
            }

            // Type t;
            // Exp e;
            public Integer visit(ExpNewArray n) {
                n.e.accept(staticEval);
                return null;
            }

            // int i;
            public Integer visit(ExpInteger n) {
                return n.i;
            }

            public Integer visit(ExpTrue n) {
                return 1;
            }

            public Integer visit(ExpFalse n) {
                return 0;
            }

            // Var v;
            public Integer visit(ExpVar n) {
                if (isUndefined(n.v.id)) {
                    throw new StaticAnalysisException("Local variable may not have been assigned: " + n.v.id, n.getTags());
                } else {
                    return null;
                }
            }

            private Integer udNot(Integer i) {
                return eq(i, TRUE) ? FALSE : eq(i, FALSE) ? TRUE : UNKNOWN;
            }

            private Integer udEq(Integer i1, Integer i2) {
                if (i1 == null || i2 == null) {
                    return UNKNOWN;
                } else {
                    return i1.equals(i2) ? TRUE : FALSE;
                }
            }

            private Integer udMult(Integer i1, Integer i2) {
                if (i1 == null || i2 == null) {
                    return UNKNOWN;
                } else {
                    return i1 * i2;
                }
            }

            private Integer udMinus(Integer i1, Integer i2) {
                if (i1 == null || i2 == null) {
                    return UNKNOWN;
                } else {
                    return i1 - i2;
                }
            }

            private Integer udAdd(Integer i1, Integer i2) {
                if (i1 == null || i2 == null) {
                    return UNKNOWN;
                } else {
                    return i1 + i2;
                }
            }

            private Integer udLt(Integer i1, Integer i2) {
                if (i1 == null || i2 == null) {
                    return UNKNOWN;
                } else {
                    return i1 < i2 ? TRUE : FALSE;
                }
            }

            // Exp e;
            public Integer visit(ExpNot n) {
                return udNot(n.e.accept(staticEval));
            }

            // Exp e;
            public Integer visit(ExpIsnull n) {
                n.e.accept(staticEval);
                return null;
            }

            // Exp e1, e2;
            // ExpOp.Op op;
            public Integer visit(ExpOp n) {
                Integer v1 = n.e1.accept(staticEval);
                Integer v2;
                switch (n.op) {
                    case AND:
                        if (eq(v1, FALSE)) {
                            return FALSE;
                        }
                        v2 = n.e2.accept(staticEval);
                        if (eq(v1, TRUE)) {
                            return v2;
                        }
                        return null;
                    case LESSTHAN:
                        v2 = n.e2.accept(staticEval);
                        return udLt(v1, v2);
                    case EQUALS:
                        v2 = n.e2.accept(staticEval);
                        return udEq(v1, v2);
                    case DIV:
                        v2 = n.e2.accept(staticEval);
                        return null;
                    case PLUS:
                        v2 = n.e2.accept(staticEval);
                        return udAdd(v1, v2);
                    case MINUS:
                        v2 = n.e2.accept(staticEval);
                        return udMinus(v1, v2);
                    case TIMES:
                        v2 = n.e2.accept(staticEval);
                        return udMult(v1, v2);
                    default:
                        throw new Error("Unknown operator: " + n.op);
                }
            }
        };
    }

    private void pushDef() {
        defStack.push(new HashMap<>());
    }

    private Map<String, Boolean> popDef() {
        return defStack.pop();
    }

    private boolean eq(Integer i1, Integer i2) {
        if (i1 == null) {
            return i2 == null;
        } else if (i2 == null) {
            return false;
        } else {
            return i1.intValue() == i2.intValue();
        }
    }

    private <T> Set<T> intersect(Set<T> s1, Set<T> s2) {
        Set<T> inBoth = new HashSet<>();
        for (T x : s1) {
            if (s2.contains(x)) {
                inBoth.add(x);
            }
        }
        return inBoth;
    }

    private void doDefs(Collection<String> defined) {
        for (String v : defined) {
            doDef(v);
        }
    }

    /**
     * Record in the definitions stack that local variable v has been assigned a
     * value. This should only be called for a local variable which is currently
     * explicitly represented on the definitions stack.
     *
     * @param v the variable name
     *
     * @see isUndefined(String)
     */
    private void doDef(String v) {
        for (Map<String, Boolean> defs : defStack) {
            if (defs.keySet().contains(v)) {
                defs.put(v, true);
                return;
            }
        }
        throw new Error("Unexpected variable in use-def check: " + v);
    }

    private void undoDefs(Collection<String> vs) {
        for (String v : vs) {
            undoDef(v);
        }
    }

    private void undoDef(String v) {
        for (Map<String, Boolean> defs : defStack) {
            if (defs.keySet().contains(v)) {
                defs.put(v, false);
                return;
            }
        }
        throw new Error("Unexpected variable in use-def check: " + v);
    }

    private void addUndef(String v) {
        defStack.peek().put(v, false);
    }

    private void addDefsForFormals(Collection<Formal> fs) {
        for (Formal f : fs) {
            defStack.peek().put(f.id, true);
        }
    }

    private boolean isUndefined(String v) {
        for (Map<String, Boolean> defs : defStack) {
            if (defs.keySet().contains(v)) {
                return !defs.get(v);
            }
        }
        // if we get here, it must be a field
        return false;
    }

    private void removeLocals(Collection<String> vars) {
        Set<String> locals = defStack.peek().keySet();
        vars.removeAll(locals);
    }

    // ProcDecl pd;
    // List<FunDecl> fds;
    public Set<String> visit(Program n) {
        n.pd.accept(this);
        for (MethodDecl md : n.mds) {
            md.accept(this);
        }
        return null;
    }

    private Set<String> sequenceUpdates(List<Stm> ss) {
        Set<String> updates = new HashSet<>();
        for (Stm s : ss) {
            updates.addAll(s.accept(this));
            if (blackHole) {
                break;
            }
        }
        return updates;
    }

    // Type t;
    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    // Exp e;
    public Set<String> visit(FunDecl n) {
        blackHole = false;
        pushDef();
        addDefsForFormals(n.fs);
        sequenceUpdates(n.ss);
        if (!blackHole) {
            n.e.accept(staticEval);
        }
        // leaving method scope
        popDef();
        return null;
    }

    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    public Set<String> visit(ProcDecl n) {
        blackHole = false;
        pushDef();
        addDefsForFormals(n.fs);
        sequenceUpdates(n.ss);
        // leaving method scope
        popDef();
        return null;
    }

    // List<Stm> ss;
    public Set<String> visit(StmBlock n) {
        pushDef();
        Set<String> updates = sequenceUpdates(n.ss);
        // leaving block scope
        removeLocals(updates);
        popDef();
        return updates;
    }

    // Type t;
    // String id;
    public Set<String> visit(StmVarDecl n) {
        addUndef(n.id);
        return new HashSet<>();
    }

    // Exp e;
    // Stm st, sf;
    public Set<String> visit(StmIf n) {
        Integer cond = n.e.accept(staticEval);
        Set<String> updates;
        if (eq(cond, TRUE)) {
            updates = n.st.accept(this);
        } else if (eq(cond, FALSE)) {
            updates = n.sf.accept(this);
        } else {
            Set<String> trueBranchUpdates = n.st.accept(this);
            undoDefs(trueBranchUpdates);
            if (blackHole) {
                blackHole = false;
                updates = n.sf.accept(this);
            } else {
                Set<String> falseBranchUpdates = n.sf.accept(this);
                undoDefs(falseBranchUpdates);
                if (blackHole) {
                    blackHole = false;
                    updates = trueBranchUpdates;
                } else {
                    updates = intersect(trueBranchUpdates, falseBranchUpdates);
                }
                doDefs(updates);
            }
        }
        return updates;
    }

    // Exp e;
    // Stm body;
    public Set<String> visit(StmWhile n) {
        Integer cond = n.e.accept(staticEval);
        Set<String> updates;
        if (eq(cond, TRUE)) {
            updates = n.body.accept(this);
            blackHole = true;
        } else if (eq(cond, FALSE)) {
            updates = new HashSet<>();
        } else {
            Set<String> trueDefs = n.body.accept(this);
            undoDefs(trueDefs);
            updates = new HashSet<>();
        }
        return updates;
    }

    // Exp e;
    public Set<String> visit(StmOutput n) {
        n.e.accept(staticEval);
        return new HashSet<>();
    }

    // Exp e;
    public Set<String> visit(StmOutchar n) {
        n.e.accept(staticEval);
        return new HashSet<>();
    }

    // Var v;
    // Exp e;
    public Set<String> visit(StmAssign n) {
        Set<String> updates = new HashSet<>();
        n.e.accept(staticEval);
        if (isUndefined(n.v.id)) {
            doDef(n.v.id);
            updates.add(n.v.id);
        }
        return updates;
    }

    // Exp e1,e2,e3;
    public Set<String> visit(StmArrayAssign n) {
        n.e1.accept(staticEval);
        n.e2.accept(staticEval);
        n.e3.accept(staticEval);
        return new HashSet<>();
    }
    
    // String id;
    // List<Exp> es;
    public Set<String> visit(StmCall n) {
        for (Exp e : n.es) {
            e.accept(staticEval);
        }
        return new HashSet<>();
    }

}
