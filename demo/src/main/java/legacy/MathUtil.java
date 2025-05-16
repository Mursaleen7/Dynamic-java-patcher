package legacy;

/**
 * Simulated legacy class for demonstration purposes
 */
public class MathUtil {
    /**
     * Deprecated method for adding numbers
     * @deprecated Use java.lang.Math.addExact instead
     */
    @Deprecated
    public static int sum(int a, int b) {
        return a + b;
    }
} 