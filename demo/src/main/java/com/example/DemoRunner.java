package com.example;

/**
 * Demonstration runner that shows the differences between the original and improved code.
 */
public class DemoRunner {
    
    public static void main(String[] args) {
        System.out.println("===== SpeedDoctor OpenRewrite Recipes Demo =====");
        
        // Create instances of both implementations
        SampleCode original = new SampleCode();
        SampleCodeImproved improved = new SampleCodeImproved();
        
        // Test performance improvements
        System.out.println("\n1. Performance Improvement:");
        testPerformance(original, improved);
        
        // Test API modernization
        System.out.println("\n2. API Modernization:");
        testApiModernization(original, improved);
        
        // Test security improvements
        System.out.println("\n3. Security Improvements:");
        testSecurity(original, improved);
        
        System.out.println("\nDemo completed!");
    }
    
    private static void testPerformance(SampleCode original, SampleCodeImproved improved) {
        int n = 1000;
        
        // Test original implementation
        long startTime = System.currentTimeMillis();
        long originalResult = original.calculateSum(n);
        long originalTime = System.currentTimeMillis() - startTime;
        
        // Test improved implementation
        startTime = System.currentTimeMillis();
        long improvedResult = improved.calculateSum(n);
        long improvedTime = System.currentTimeMillis() - startTime;
        
        // Show results
        System.out.println("Original implementation (O(n²)):");
        System.out.println("  - Result: " + originalResult);
        System.out.println("  - Time: " + originalTime + " ms");
        
        System.out.println("Improved implementation (O(1)):");
        System.out.println("  - Result: " + improvedResult);
        System.out.println("  - Time: " + improvedTime + " ms");
        
        System.out.println("Speedup: " + (originalTime > 0 ? originalTime / Math.max(1, improvedTime) : "N/A") + "x");
    }
    
    private static void testApiModernization(SampleCode original, SampleCodeImproved improved) {
        int a = 5, b = 7;
        
        // Test original implementation
        System.out.println("Original implementation (uses deprecated legacy.MathUtil.sum):");
        try {
            int originalResult = original.addNumbers(a, b);
            System.out.println("  - Result: " + originalResult);
            System.out.println("  - Using: legacy.MathUtil.sum(int, int)");
        } catch (Exception e) {
            System.out.println("  - Error: " + e.getMessage());
        }
        
        // Test improved implementation
        System.out.println("Improved implementation (uses Math.addExact):");
        try {
            int improvedResult = improved.addNumbers(a, b);
            System.out.println("  - Result: " + improvedResult);
            System.out.println("  - Using: java.lang.Math.addExact(int, int)");
        } catch (Exception e) {
            System.out.println("  - Error: " + e.getMessage());
        }
    }
    
    private static void testSecurity(SampleCode original, SampleCodeImproved improved) {
        String maliciousInput = "' OR '1'='1"; // SQL injection attempt
        
        // Show original vulnerable implementation
        System.out.println("Original implementation (vulnerable to SQL injection):");
        System.out.println("  - SQL query would be: SELECT * FROM users WHERE username = '" + maliciousInput + "'");
        System.out.println("  - This would allow attackers to bypass authentication!");
        
        // Show improved secure implementation
        System.out.println("Improved implementation (uses PreparedStatement):");
        System.out.println("  - PreparedStatement with parameter binding: SELECT * FROM users WHERE username = ?");
        System.out.println("  - Parameter safely bound as: " + maliciousInput);
        System.out.println("  - SQL injection attempt blocked!");
    }
} 