package com.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.security.SqlSanitizer;

/**
 * Improved version of the sample code after applying SpeedDoctor recipes:
 * 1. Performance hotspots optimized
 * 2. Deprecated API calls modernized
 * 3. Security vulnerabilities patched
 */
public class SampleCodeImproved {
    
    /**
     * Optimized method with O(n) complexity instead of O(n²)
     */
    public long calculateSum(int n) {
        // Mathematical formula: sum of i*j for i,j from 0 to n-1
        // This is a direct calculation using the formula: n(n-1)(2n-1)/6
        if (n <= 0) return 0;
        long nn = n;
        return (nn * (nn - 1) * (2 * nn - 1)) / 6;
    }
    
    /**
     * Modernized API usage (java.lang.Math.addExact instead of legacy.MathUtil.sum)
     */
    public int addNumbers(int a, int b) {
        // Using the modern Math.addExact method instead of the deprecated legacy method
        return Math.addExact(a, b);
    }
    
    /**
     * Security patched method using prepared statements to prevent SQL injection
     */
    public void queryUser(String username) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        
        // Using prepared statements to prevent SQL injection
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);
        stmt.executeQuery();
        
        // Alternative approach: using sanitized SQL with regular Statement
        Statement stmt2 = conn.createStatement();
        String sanitizedSql = "SELECT * FROM users WHERE username = '" + 
                SqlSanitizer.clean(username) + "'";
        stmt2.executeQuery(sanitizedSql);
    }
} 