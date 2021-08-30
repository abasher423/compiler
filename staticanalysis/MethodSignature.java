package mapl.staticanalysis;

import java.util.ArrayList;
import java.util.List;
import mapl.ast.Type;

/**
 * Mapl method signatures.
 */
public class MethodSignature {

    private final String methodName;
    private final Type returnType; // null for procedures
    private final List<Type> paramTypes;

    public MethodSignature(String methodName, Type returnType, List<Type> formalTypes) {
        this.methodName = methodName;
        this.returnType = returnType;
        paramTypes = new ArrayList<Type>();
        for (Type t : formalTypes) {
            paramTypes.add(t);
        }
    }

    public MethodSignature(String methodName, List<Type> formalTypes) {
        this(methodName, null, formalTypes);
    }

    /**
     * The name of this method.
     *
     * @return the method name
     */
    public String getName() {
        return methodName;
    }

    /**
     * The return type of this method.
     *
     * @return the return type
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * The type of a given parameter.
     *
     * @param i the index of the parameter in this method's parameter list
     * (indexes start at zero)
     * @return the type for the specified parameter
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Type getParamType(int i) {
        return paramTypes.get(i);
    }

    /**
     * The arity (number of parameters) of this method.
     *
     * @return the arity of this method
     */
    public int getArity() {
        return paramTypes.size();
    }
}