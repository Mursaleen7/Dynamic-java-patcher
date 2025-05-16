import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Demo application to showcase SpeedDoctor's capabilities:
 * 1. Real-time lightweight profiler (finds performance hotspots)
 * 2. Instant deprecation rescue (provides runtime fallbacks)
 * 3. Zero-downtime security patches (adds runtime sanitization)
 */
public class SpeedDoctorDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("===== SpeedDoctor Demo Application =====");
        
        // Call our demo methods
        System.out.println("\n1. Testing Performance Hotspot Detection:");
        performanceHotspot();
        
        System.out.println("\n2. Testing Deprecated API Fallback:");
        useDeprecatedMethod();
        
        System.out.println("\n3. Testing Security Vulnerability Protection:");
        securityVulnerability();
        
        System.out.println("\nDemo completed! Check logs for SpeedDoctor interventions.");
    }
    
    /**
     * This method has a performance issue (O(n²) complexity) that should be detected
     * by the profiler as a hotspot.
     */
    private static void performanceHotspot() {
        System.out.println("Running inefficient algorithm...");
        
        // An inefficient O(n²) algorithm that will be flagged as a hotspot
        int n = 1000;
        long sum = 0;
        
        // Start timing
        long startTime = System.currentTimeMillis();
        
        // Inefficient nested loop (O(n²))
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum += (i * j);
            }
        }
        
        // End timing
        long endTime = System.currentTimeMillis();
        
        System.out.println("Computed sum: " + sum);
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }
    
    /**
     * This method uses a deprecated API that should be rescued at runtime
     * by the deprecation rescue feature.
     */
    private static void useDeprecatedMethod() {
        System.out.println("Calling deprecated API...");
        
        // Call to a "deprecated" legacy method that should be rescued
        // In a real scenario, this would be a genuine deprecated method
        String result = LegacyClass.oldMethod("test input");
        
        System.out.println("Result from legacy API: " + result);
    }
    
    /**
     * This method has a potential security vulnerability (SQL injection)
     * that should be protected by the security patch feature.
     */
    private static void securityVulnerability() {
        System.out.println("Testing SQL injection vulnerability...");
        
        // Simulate a malicious SQL injection attempt
        String userInput = "'; DROP TABLE users; --";
        
        try {
            // This should be caught and sanitized by the security patch feature
            executeSqlQuery("SELECT * FROM users WHERE username = '" + userInput + "'");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * This method is vulnerable to SQL injection.
     */
    private static void executeSqlQuery(String sql) {
        System.out.println("Executing SQL: " + sql);
        // In a real app, this would connect to a database and execute the query
        // Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        // Statement stmt = conn.createStatement();
        // stmt.executeQuery(sql);
    }
}

/**
 * A legacy class with a deprecated method that should be rescued.
 */
class LegacyClass {
    /**
     * This is a "deprecated" method that should be rescued at runtime.
     * In a real scenario, this would be annotated with @Deprecated.
     */
    public static String oldMethod(String input) {
        return "Legacy implementation: " + input;
    }
}

/**
 * A modern class with the new implementation of the deprecated method.
 */
class ModernClass {
    /**
     * This is the new method that should be used instead of the deprecated one.
     */
    public static String newMethod(String input) {
        return "Modern implementation: " + input;
    }
} 