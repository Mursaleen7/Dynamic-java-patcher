package com.example.security;

import java.util.regex.Pattern;

/**
 * SQL sanitization utility to prevent SQL injection attacks.
 */
public final class SqlSanitizer {
    private static final Pattern SQL_INJECTION_PATTERN = 
            Pattern.compile("(?i)('\\s*or\\s*'\\s*=\\s*')|('\\s*or\\s*1\\s*=\\s*1)|(;\\s*drop\\s+table)|(;\\s*delete\\s+from)|(--\\s*$)");
    
    private SqlSanitizer() {
        // Utility class, no instances
    }
    
    /**
     * Clean SQL input to prevent injection attacks.
     */
    public static String clean(String input) {
        if (input == null) {
            return null;
        }
        return SQL_INJECTION_PATTERN.matcher(input).replaceAll("");
    }
} 