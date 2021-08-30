package mapl.ast;

public class Var extends AST {

    public final String id;

    public Var(String aid) {
        id = aid;
    }
    
    /**
     * Assigned in a preliminary pass by the compiler.
     * For a local/parameter, this is the stack frame offset.
     * @see staticanalysis.VarAllocator
     */
    public int offset;
    
    /**
     * Assigned in a preliminary pass by the interpreter.
     * Set to false for fields, true for locals/parameters.
     */
    public boolean isStackAllocated;
    
    @Override
    public String toString() {
        return id;
    }
}
