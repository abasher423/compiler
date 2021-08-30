package mapl.staticanalysis;

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
import mapl.ast.Type;
import mapl.ast.TypeInt;
import mapl.ast.ExpNewArray;
import mapl.ast.ExpNot;
import mapl.ast.ExpTrue;
import mapl.ast.Var;
import mapl.ast.TypeArray;
import mapl.ast.StmWhile;
import mapl.ast.FunDecl;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import mapl.ast.util.Visitor;
import mapl.ast.util.VisitorAdapter;

/**
 * Visitors for type checking Mapl programs.
 */
public class TypeChecker extends VisitorAdapter<Type> {

    private static final Type TYPE_BOOLEAN = new TypeBoolean();
    private static final Type TYPE_INT = new TypeInt();

    /**
     * A symbol table of method signatures.
     */
    private SymbolTable symbolTable;
    
    /**
     * A stack of tables mapping the local variables/parameters currently in
     * scope to their types.
     */
    private Deque<Map<String, Type>> locals;

    /**
     * Initialise a new type check visitor.
     *
     * @param s the symbol table to use during type checking
     */
    public TypeChecker(SymbolTable s) {
        symbolTable = s;
        locals = new LinkedList<>();
    }

    /**
     * ***************
     */
    /* Helper methods */
    /**
     * ***************
     */
    /**
     *
     */
    private void pushLocals() {
        locals.push(new HashMap<String, Type>());
    }

    /**
     * Exit the current local variable scope.
     */
    private void popLocals() {
        locals.pop();
    }

    /**
     * Add an entry to the table of local variables/parameters currently in
     * scope.
     *
     * @param name the name of the local variable/parameter
     * @param type the type
     * @param tags tags for error reporting
     *
     * @throws TypeCheckingException if a local variable/parameter entry with
     * the same name already exists in the closest current scope
     */
    private void addLocal(String name, Type type, List<String> tags) {
        // we check only if name is already declared in the <b>closest</b> scope
        // region so (unlike Java) we allow a local variable declaration
        // in an inner block to hide a declaration of the same name in
        // an enclosing outer block
        if (locals.peek().containsKey(name)) {
            throw new TypeCheckingException("Double declaration of local var/param: " + name, tags);
        }
        locals.peek().put(name, type);
    }

    /**
     * Lookup the type for a variable.
     *
     * @param name the variable name
     * @return the type of the variable
     * @throws TypeCheckingException if the variable is not in scope
     */
    private Type getTypeForVar(Var v) {
        Type t;
        String name = v.id;
        for (Map<String, Type> scope : locals) {
            t = scope.get(name);
            if (t != null) {
                return t;
            }
        }
        throw new TypeCheckingException("No declaration found for variable: " + name, v.getTags());
    }
    
    private Type checkCall(List<String> tags, String mname, List<Exp> actuals) {
        String msgName = mname;
        
        // check that named method exists in symbol table
        MethodSignature calledMethod = symbolTable.getMethodSignature(mname);
        if (calledMethod == null) {
            throw new TypeCheckingException("Method " + msgName + " is not defined", tags);
        }

        // check that arities match
        int arity = calledMethod.getArity();
        if (arity != actuals.size()) {
            throw new TypeCheckingException("Method " + msgName
                    + " called with wrong number of parameters"
                    + " (" + actuals.size() + " instead of " + arity + ")", tags);
        }

        // check actual parameter types against formal parameter types
        for (int i = 0; i < arity; i++) {
            Type t1 = calledMethod.getParamType(i);
            Type t2 = actuals.get(i).accept(this);
            if (!t1.equals(t2)) {
                throw new TypeCheckingException(ordinalString(i+1) + " actual parameter has wrong type for "
                        + msgName, actuals.get(i).getTags());
            }
        }

        // if we get this far, it is a valid method call, return the method's
        // return type (which will be null for procedures)
        return calledMethod.getReturnType();
    }
    
    private String ordinalString(int i) {
        switch (i) {
            case 11: case 12: case 13:
                return i + "th";
            default:
                switch (i % 10) {
                    case 1:
                        return i + "st";
                    case 2:
                        return i + "nd";
                    case 3:
                        return i + "rd";
                    default:
                        return i + "th";
                }
        }
    }

    /**
     * *****************
     */
    /* Visitor methods. */
    /**
     * *****************
     */
    
    // ProcDecl pd;
    // List<MethodDecl> mds;
    public Type visit(Program n) {
        n.pd.accept(this);
        for (MethodDecl md : n.mds) {
            md.accept(this);
        }
        return null;
    }

    // Type t;
    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    // Exp e;
    @Override
    public Type visit(FunDecl n) {
        // open new variable scope for the parameters
        pushLocals();
        for (Formal f : n.fs) {
            addLocal(f.id, f.t, f.getTags());
        }
        // open new variable scope for the local variables
        pushLocals();
        // check the body statements
        for (Stm s : n.ss) {
            s.accept(this);
        }
        // check the return expression
        Type retType = n.t;
        if (!retType.equals(n.e.accept(this))) {
            throw new TypeCheckingException("Return expression has wrong type for method " + n.id, n.e.getTags());
        }
        popLocals(); // close local variable scope
        popLocals(); // close parameter scope
        return null;
    }

    // String id;
    // List<Formal> fs;
    // List<Stm> ss;
    @Override
    public Type visit(ProcDecl n) {
        // open new variable scope for the parameters
        pushLocals();
        for (Formal f : n.fs) {
            addLocal(f.id, f.t, f.getTags());
        }
        // open new variable scope for the local variables
        pushLocals();
        // check the body statements
        for (Stm s : n.ss) {
            s.accept(this);
        }
        popLocals(); // close local variable scope
        popLocals(); // close parameter scope
        return null;
    }

    /*======================================*/
    /* Statement visitors (all return null) */
    /*======================================*/
    
    // Type t
    // String id
    @Override
    public Type visit(StmVarDecl n) {
        addLocal(n.id, n.t, n.getTags());
        return null;
    }
    
    // Exp e;
    // Stm st, sf;
    @Override
    public Type visit(StmIf n) {
        if (!(n.e.accept(this).equals(TYPE_BOOLEAN))) {
            throw new TypeCheckingException("The condition of if must be of type boolean", n.e.getTags());
        }
        n.st.accept(this);
        n.sf.accept(this);
        return null;
    }

    // Exp e;
    // Stm body;
    @Override
    public Type visit(StmWhile n) {
        if (!(n.e.accept(this).equals(TYPE_BOOLEAN))) {
            throw new TypeCheckingException("The condition of while must be of type boolean", n.e.getTags());
        }
        n.body.accept(this);
        return null;
    }

    // Exp e;
    @Override
    public Type visit(StmOutput n) {
        if (!(n.e.accept(this).equals(TYPE_INT))) {
            throw new TypeCheckingException("The argument of output must be of type int", n.e.getTags());
        }
        return null;
    }

    // Exp e;
    @Override
    public Type visit(StmOutchar n) {
        if (!(n.e.accept(this).equals(TYPE_INT))) {
            throw new TypeCheckingException("The argument of outchar must be of type int", n.e.getTags());
        }
        return null;
    }

    // Var v;
    // Exp e;
    @Override
    public Type visit(StmAssign n) {
        Type t1 = getTypeForVar(n.v);
        Type t2 = n.e.accept(this);
        if (!t1.equals(t2)) {
            throw new TypeCheckingException("Type error in assignment to " + n.v.id, n.e.getTags());
        }
        return null;
    }

    // Exp e1,e2,e3;
    @Override
    public Type visit(StmArrayAssign n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        Type t3 = n.e3.accept(this);
        if (t1 instanceof TypeArray) {
            Type elementType = ((TypeArray) t1).t;
            if (!(t2 instanceof TypeInt)) {
                throw new TypeCheckingException("Type error in array assignment: index is not an int", n.e2.getTags());
            }
            if (!elementType.equals(t3)) {
                throw new TypeCheckingException("Type error in array assignment: RHS type does not match array element type", n.e3.getTags());
            }
        } else {
            throw new TypeCheckingException("Type error in array assignment: not an array", n.e1.getTags());
        }
        return null;
    }

    // List<Stm> ss;
    @Override
    public Type visit(StmBlock n) {
        pushLocals();
        for (Stm s : n.ss) {
            s.accept(this);
        }
        popLocals();
        return null;
    }
    
    // String id;
    // List<Exp> es;
    @Override
    public Type visit(StmCall n) {
        checkCall(n.getTags(), n.id, n.es);
        return null;
    }

    /*=========================================*/
    /* Expression visitors (all return a Type) */
    /*=========================================*/
    
    // Exp e1, e2;
    // ExpOp.Op op;
    @Override
    public Type visit(ExpOp n) {
        Type resType;
        switch (n.op) {
            case DIV:
            case PLUS:
            case MINUS:
            case TIMES:
                resType = TYPE_INT;
                break;
            case AND:
            case LESSTHAN:
            case EQUALS:
                resType = TYPE_BOOLEAN;
                break;
            default:
                throw new Error("Unknown operator: " + n.op);
        }
        String errmsg = null;
        Exp keyExp = n;
        switch (n.op) {
            case DIV:
            case PLUS:
            case MINUS:
            case TIMES:
            case LESSTHAN:
                if (!(n.e1.accept(this).equals(TYPE_INT))) {
                    errmsg = "First argument of " + n.op + " must be of type int";
                    keyExp = n.e1;
                } else if (!(n.e2.accept(this).equals(TYPE_INT))) {
                    errmsg = "Second argument of " + n.op + " must be of type int";
                    keyExp = n.e2;
                }
                break;
            case AND:
                if (!(n.e1.accept(this).equals(TYPE_BOOLEAN))) {
                    errmsg = "First argument of " + n.op + " must be of type boolean";
                    keyExp = n.e1;
                } else if (!(n.e2.accept(this).equals(TYPE_BOOLEAN))) {
                    errmsg = "Second argument of " + n.op + " must be of type boolean";
                    keyExp = n.e2;
                }
                break;
            case EQUALS:
                Type t1 = n.e1.accept(this);
                Type t2 = n.e2.accept(this);
                if (!t1.equals(t2)) {
                    errmsg = "Arguments to == must have the same type";
                }
                break;
            default:
                throw new Error("unknown operator: " + n.op);
        }
        if (errmsg == null) {
            return resType;
        } else {
            throw new TypeCheckingException(errmsg, keyExp.getTags());
        }
    }

    // Exp e;
    // String id;
    // List<Exp> es;
    @Override
    public Type visit(ExpCall n) {
        Type retType = checkCall(n.getTags(), n.id, n.es);
        if (retType == null) {
            throw new TypeCheckingException("Proc " + n.id + " cannot be called here (it's no fun)", n.getTags());
        }
        return retType;
    }

    // int i;
    @Override
    public Type visit(ExpInteger n) {
        return TYPE_INT;
    }

    @Override
    public Type visit(ExpTrue n) {
        return TYPE_BOOLEAN;
    }

    @Override
    public Type visit(ExpFalse n) {
        return TYPE_BOOLEAN;
    }

    // Var v;
    @Override
    public Type visit(ExpVar n) {
        return getTypeForVar(n.v);
    }

    // Exp e;
    @Override
    public Type visit(ExpNot n) {
        if (!(n.e.accept(this).equals(TYPE_BOOLEAN))) {
            throw new TypeCheckingException("Negated expression must be of type boolean", n.e.getTags());
        }
        return TYPE_BOOLEAN;
    }

    // Exp e1,e2;
    @Override
    public Type visit(ExpArrayLookup n) {
        Type t1 = n.e1.accept(this);
        Type t2 = n.e2.accept(this);
        if (t1 instanceof TypeArray) {
            Type elementType = ((TypeArray) t1).t;
            if (!(t2 instanceof TypeInt)) {
                throw new TypeCheckingException("Type error in array lookup: index is not an int", n.e2.getTags());
            }
            return elementType;
        } else {
            throw new TypeCheckingException("Type error in array lookup: not an array", n.e1.getTags());
        }
    }

    // Exp e;
    @Override
    public Type visit(ExpArrayLength n) {
        Type t = n.e.accept(this);
        if (t instanceof TypeArray) {
            return TYPE_INT;
        } else {
            throw new TypeCheckingException("Type error in array length: not an array", n.e.getTags());
        }
    }

    // Type t;
    // Exp e;
    @Override
    public Type visit(ExpNewArray n) {
        Type sizeType = n.e.accept(this);
        if (!(sizeType instanceof TypeInt)) {
            throw new TypeCheckingException("Type error in array creation: size is not an int", n.e.getTags());
        }
        return new TypeArray(n.t);
    }

    // Exp e;
    @Override
    public Type visit(ExpIsnull n) {
        Type t = n.e.accept(this);
        if (t instanceof TypeArray) {
            return TYPE_BOOLEAN;
        } else {
            throw new TypeCheckingException("Type error in null test: not an array", n.getTags());
        }
    }
}
