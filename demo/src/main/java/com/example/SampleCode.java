package com.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample code with issues that OpenRewrite recipes can fix:
 * 1. Performance hotspots (inefficient algorithm)
 * 2. Deprecated API usage
 * 3. Security vulnerabilities (SQL injection)
 */
public class SampleCode {
    
    /**
     * Inefficient O(n²) method that should be optimized
     */
    public long calculateSum(int n) {
        long sum = 0;
        // Inefficient nested loops
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum += (i * j);
            }
        }
        return sum;
    }
    
    /**
     * Uses a deprecated API (legacy.MathUtil.sum)
     */
    public int addNumbers(int a, int b) {
        // Using a "legacy" class that's marked as deprecated
        return legacy.MathUtil.sum(a, b);
    }
    
    /**
     * Has a SQL injection vulnerability
     */
    public void queryUser(String username) throws Exception {
        // Vulnerable to SQL injection
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement stmt = conn.createStatement();
        
        // This is vulnerable to SQL injection
        String sql = "SELECT * FROM users WHERE username = '" + username + "'";
        stmt.executeQuery(sql);
    }
} 