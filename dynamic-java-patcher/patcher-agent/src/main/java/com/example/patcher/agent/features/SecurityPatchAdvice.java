package com.example.patcher.agent.features;

import net.bytebuddy.asm.Advice;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Advice for adding security patches to vulnerable methods.
 * This includes sanitization of SQL and HTTP inputs.
 */
public class SecurityPatchAdvice {
    private static final Logger LOGGER = Logger.getLogger(SecurityPatchAdvice.class.getName());
    
    // SQL injection patterns - initialized with default value, can be updated at runtime
    private static volatile Pattern SQL_INJECTION_PATTERN = 
            Pattern.compile("(?i)('\\s*or\\s*'\\s*=\\s*')|('\\s*or\\s*1\\s*=\\s*1)|"
                   + "(;\\s*drop\\s+table)|(;\\s*delete\\s+from)|(--\\s*$)");
    
    // XSS attack patterns - initialized with default value, can be updated at runtime
    private static volatile Pattern XSS_PATTERN = 
            Pattern.compile("<script>|<\\/script>|javascript:|onerror=|onclick=|onload=");
    
    /**
     * Update the SQL injection pattern at runtime.
     * 
     * @param pattern The new pattern to use
     */
    public static void setSqlInjectionPattern(Pattern pattern) {
        SQL_INJECTION_PATTERN = pattern;
    }
    
    /**
     * Update the XSS pattern at runtime.
     * 
     * @param pattern The new pattern to use
     */
    public static void setXssPattern(Pattern pattern) {
        XSS_PATTERN = pattern;
    }
    
    /**
     * Sanitizes SQL queries to prevent SQL injection attacks.
     */
    @Advice.OnMethodEnter
    public static void sanitizeSql(@Advice.Argument(value = 0, readOnly = false) String sql) {
        if (sql == null) {
            return;
        }
        
        if (SQL_INJECTION_PATTERN.matcher(sql).find()) {
            LOGGER.warning("[SecurityPatch] Potential SQL injection detected: " + sql);
            
            // Sanitize by replacing dangerous patterns
            String sanitized = SQL_INJECTION_PATTERN.matcher(sql).replaceAll("");
            
            // Update the argument with sanitized version
            sql = sanitized;
        }
    }
    
    /**
     * Sanitizes HTTP request parameters to prevent XSS attacks.
     */
    public static class HttpSanitizer {
        @Advice.OnMethodEnter
        public static void sanitizeParam(@Advice.Argument(value = 0, readOnly = false) String parameter) {
            if (parameter == null) {
                return;
            }
            
            if (XSS_PATTERN.matcher(parameter).find()) {
                LOGGER.warning("[SecurityPatch] Potential XSS attack detected: " + parameter);
                
                // Sanitize by escaping HTML
                String sanitized = escapeHtml(parameter);
                
                // Replace the original parameter with sanitized version
                parameter = sanitized;
            }
        }
    }
    
    /**
     * Sanitizes arrays of HTTP request parameters to prevent XSS attacks.
     */
    public static class HttpArraySanitizer {
        @Advice.OnMethodExit
        public static void sanitizeParamArray(@Advice.Return(readOnly = false) String[] values) {
            if (values == null || values.length == 0) {
                return;
            }
            
            boolean modified = false;
            
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (value != null && XSS_PATTERN.matcher(value).find()) {
                    LOGGER.warning("[SecurityPatch] Potential XSS attack detected in parameter array: " + value);
                    
                    // Sanitize by escaping HTML
                    values[i] = escapeHtml(value);
                    modified = true;
                }
            }
            
            if (modified) {
                LOGGER.info("[SecurityPatch] Sanitized parameter array values");
            }
        }
    }
    
    /**
     * Escape HTML special characters to prevent XSS attacks.
     * 
     * @param input The input string to escape
     * @return The escaped string
     */
    private static String escapeHtml(String input) {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
            .replace("/", "&#x2F;")
            .replace("`", "&#x60;");
    }
    
    /**
     * Utility class that can be called directly from application code
     * after the patch is applied with OpenRewrite.
     */
    public static final class SqlSanitizer {
        /**
         * Sanitizes a SQL query string to prevent SQL injection.
         * 
         * @param query The SQL query to sanitize
         * @return The sanitized query
         */
        public static String clean(String query) {
            if (query == null) {
                return null;
            }
            
            return SQL_INJECTION_PATTERN.matcher(query).replaceAll("");
        }
    }
    
    /**
     * Utility class for HTML escaping that can be called directly from application code.
     */
    public static final class HtmlEscaper {
        /**
         * Escapes HTML special characters to prevent XSS attacks.
         * 
         * @param input The input string to escape
         * @return The escaped string
         */
        public static String escape(String input) {
            if (input == null) {
                return null;
            }
            
            return escapeHtml(input);
        }
    }
} 