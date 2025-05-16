/**
 * A simplified demo of the SpeedDoctor functionality using direct class transformations
 * rather than relying on agent attachment.
 */
public class SimpleAgentDemo {
    
    public static void main(String[] args) {
        System.out.println("===== SpeedDoctor Simplified Demo =====");
        
        // Setup output directory for profiler data
        setupOutputDir();
        
        // 1. Demonstrate performance profiling
        System.out.println("\n1. Performance Profiling:");
        testPerformanceProfile();
        
        // 2. Demonstrate API deprecation fallback
        System.out.println("\n2. API Deprecation Rescue:");
        testDeprecationRescue();
        
        // 3. Demonstrate security vulnerability protection
        System.out.println("\n3. Security Vulnerability Protection:");
        testSecurityProtection();
        
        System.out.println("\nDemo completed!");
    }
    
    private static void setupOutputDir() {
        // Create profiler-data directory if it doesn't exist
        java.io.File dir = new java.io.File("profiler-data");
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Created profiler-data directory for output");
        }
    }
    
    /**
     * Demonstrates performance profiling
     */
    private static void testPerformanceProfile() {
        System.out.println("Running performance test (writing to profiler-data/hotspots.csv)...");
        
        // Example performance bottleneck
        long startTime = System.currentTimeMillis();
        
        // An inefficient O(n²) algorithm that would be flagged as a hotspot
        int n = 1000;
        long sum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum += (i * j);
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Output result and timing
        System.out.println("Computed sum: " + sum);
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
        
        // Write a sample hotspot entry to the CSV file
        try {
            java.io.FileWriter writer = new java.io.FileWriter("profiler-data/hotspots.csv");
            writer.write("Method,ExecutionTime,CallCount,AvgTimePerCall\n");
            writer.write("SimpleAgentDemo.testPerformanceProfile," + (endTime - startTime) + ",1," + (endTime - startTime) + "\n");
            writer.close();
            
            System.out.println("Wrote hotspot data to profiler-data/hotspots.csv");
        } catch (java.io.IOException e) {
            System.err.println("Error writing hotspot data: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrates API deprecation rescue
     */
    private static void testDeprecationRescue() {
        System.out.println("Testing deprecated API fallback...");
        
        // Original call to deprecated method
        String oldResult = LegacyClass.oldMethod("test input");
        System.out.println("Original (Legacy) result: " + oldResult);
        
        // The result after modernization
        String modernResult = ModernClass.newMethod("test input");
        System.out.println("Modern result (what the agent would redirect to): " + modernResult);
        
        // In a real environment with the agent, calling oldMethod would return modernResult
        System.out.println("With agent, LegacyClass.oldMethod() would return: " + modernResult);
    }
    
    /**
     * Demonstrates security vulnerability protection
     */
    private static void testSecurityProtection() {
        System.out.println("Testing security vulnerability protection...");
        
        // Example SQL injection payload
        String maliciousInput = "'; DROP TABLE users; --";
        String sqlQuery = "SELECT * FROM users WHERE username = '" + maliciousInput + "'";
        
        System.out.println("Original SQL query with injection: " + sqlQuery);
        
        // Sanitized version that the agent would produce
        String sanitizedQuery = sanitizeSql(sqlQuery);
        System.out.println("Sanitized SQL query that agent would produce: " + sanitizedQuery);
    }
    
    /**
     * Simple SQL sanitization example
     */
    private static String sanitizeSql(String sql) {
        // Remove common SQL injection patterns
        String pattern = "(?i)('\\s*or\\s*'\\s*=\\s*')|(;\\s*drop\\s+table)|(;\\s*delete\\s+from)|(--\\s*$)";
        return sql.replaceAll(pattern, "[SANITIZED]");
    }
}

/**
 * Legacy class with deprecated methods
 */
class LegacyClass {
    /**
     * This is a "deprecated" method that should be rescued at runtime
     */
    public static String oldMethod(String input) {
        return "Legacy implementation: " + input;
    }
}

/**
 * Modern class with updated methods
 */
class ModernClass {
    /**
     * This is the new method that should be used instead of the deprecated one
     */
    public static String newMethod(String input) {
        return "Modern implementation: " + input;
    }
} 