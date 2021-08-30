package mapl.compiler;

/**
 * Fresh name generator.
 */
public class FreshNameGenerator {
    
    private static int count = 0;
    
    private FreshNameGenerator(){}

    /**
     * Generate a new name. Each call to this method is guaranteed to return a
     * name distinct from all those returned by any previous calls.
     * All fresh names end with a non-empty sequence of digits.
     * @param prefix the name will start with this string
     * @return a new name
     */
    public static String makeName(String prefix) {
        return prefix + makeName();
    }

    /**
     * Generate a new name. Each call to this method is guaranteed to return a
     * name distinct from all those returned by any previous calls..
     * All fresh names end with a non-empty sequence of digits.
     * @return a new name
     */
    public static String makeName() {
        String name = "@" + count;
        ++count;
        return name;
    }

}
