package com.example.patcher.recipes.speeddoctor;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Recipe that adds security patches by sanitizing inputs in vulnerable methods.
 */
public class SecurityPatchRecipe extends Recipe {
    
    private final Map<String, String> securityPatterns = new HashMap<>();
    
    public SecurityPatchRecipe() {
        // Try to load patterns from config file
        loadSecurityPatterns();
    }
    
    /**
     * Try to load security patterns from the configuration file.
     */
    private void loadSecurityPatterns() {
        try {
            Path path = Paths.get("config", "security-patterns.json");
            
            if (Files.exists(path)) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> patterns = mapper.readValue(
                        path.toFile(), 
                        mapper.getTypeFactory().constructMapType(
                                HashMap.class, String.class, String.class));
                
                securityPatterns.putAll(patterns);
                
                System.out.println("Loaded " + patterns.size() + " security patterns from config file");
            } else {
                System.out.println("Security patterns file not found at " + path + ", using defaults");
                
                // Add default patterns
                securityPatterns.put("SQL_INJECTION", 
                        "(?i)('\\s*or\\s*'\\s*=\\s*')|('\\s*or\\s*1\\s*=\\s*1)|(;\\s*drop\\s+table)|(;\\s*delete\\s+from)|(--\\s*$)");
                securityPatterns.put("XSS", 
                        "<script>|<\\/script>|javascript:|onerror=|onclick=|onload=");
            }
        } catch (IOException e) {
            System.err.println("Failed to load security patterns: " + e.getMessage());
            
            // Add default patterns
            securityPatterns.put("SQL_INJECTION", 
                    "(?i)('\\s*or\\s*'\\s*=\\s*')|('\\s*or\\s*1\\s*=\\s*1)|(;\\s*drop\\s+table)|(;\\s*delete\\s+from)|(--\\s*$)");
            securityPatterns.put("XSS", 
                    "<script>|<\\/script>|javascript:|onerror=|onclick=|onload=");
        }
    }
    
    @Override
    public String getDisplayName() {
        return "Security Patch";
    }
    
    @Override
    public String getDescription() {
        return "Adds sanitization to methods vulnerable to SQL injection and XSS attacks.";
    }
    
    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            
            /**
             * Wrap SQL queries with sanitization
             */
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
                
                // Handle java.sql.Statement.executeQuery(String)
                if (m.getSelect() != null && 
                    TypeUtils.isAssignableTo("java.sql.Statement", m.getSelect().getType()) &&
                    "executeQuery".equals(m.getSimpleName()) && 
                    m.getArguments().size() == 1) {
                    
                    // Create the utility class if it doesn't exist yet
                    createSqlSanitizerClass();
                    
                    // Add import for our sanitizer
                    maybeAddImport("com.example.security.SqlSanitizer");
                    
                    return JavaTemplate.builder("executeQuery(SqlSanitizer.clean(#{any()}))")
                            .contextSensitive()
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(),
                                   m.getArguments().get(0));
                }
                
                // Handle java.sql.Statement.executeUpdate(String)
                if (m.getSelect() != null && 
                    TypeUtils.isAssignableTo("java.sql.Statement", m.getSelect().getType()) &&
                    "executeUpdate".equals(m.getSimpleName()) && 
                    m.getArguments().size() == 1) {
                    
                    // Create the utility class if it doesn't exist yet
                    createSqlSanitizerClass();
                    
                    // Add import for our sanitizer
                    maybeAddImport("com.example.security.SqlSanitizer");
                    
                    return JavaTemplate.builder("executeUpdate(SqlSanitizer.clean(#{any()}))")
                            .contextSensitive()
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(),
                                   m.getArguments().get(0));
                }
                
                // Handle java.sql.Connection.prepareStatement(String)
                if (m.getSelect() != null && 
                    TypeUtils.isAssignableTo("java.sql.Connection", m.getSelect().getType()) &&
                    "prepareStatement".equals(m.getSimpleName()) && 
                    m.getArguments().size() >= 1) {
                    
                    // Create the utility class if it doesn't exist yet
                    createSqlSanitizerClass();
                    
                    // Add import for our sanitizer
                    maybeAddImport("com.example.security.SqlSanitizer");
                    
                    return JavaTemplate.builder("prepareStatement(SqlSanitizer.clean(#{any()}))")
                            .contextSensitive()
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(),
                                   m.getArguments().get(0));
                }
                
                // Handle getParameter and getParameterValues in HttpServletRequest
                if (m.getSelect() != null && 
                    TypeUtils.isAssignableTo("javax.servlet.http.HttpServletRequest", m.getSelect().getType()) &&
                    ("getParameter".equals(m.getSimpleName()) || "getParameterValues".equals(m.getSimpleName())) && 
                    m.getArguments().size() == 1) {
                    
                    // Create the HTML escaper utility
                    createHtmlEscaperClass();
                    
                    // Add import for our escaper
                    maybeAddImport("com.example.security.HtmlEscaper");
                    
                    String templateText = "getParameter".equals(m.getSimpleName()) ? 
                            "HtmlEscaper.escape(" + m.getSimpleName() + "(#{any()}))" :
                            "HtmlEscaper.escapeArray(" + m.getSimpleName() + "(#{any()}))";
                            
                    return JavaTemplate.builder(templateText)
                            .contextSensitive()
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(),
                                   m.getArguments().get(0));
                }
                
                // Handle java.io.File constructor with user input (path traversal)
                if (m.getMethodType() != null && 
                    TypeUtils.isOfClassType(m.getMethodType().getReturnType(), "java.io.File") &&
                    "<constructor>".equals(m.getSimpleName()) && 
                    m.getArguments().size() == 1) {
                    
                    // Create the path sanitizer utility
                    createPathSanitizerClass();
                    
                    // Add import for our sanitizer
                    maybeAddImport("com.example.security.PathSanitizer");
                    
                    return JavaTemplate.builder("new File(PathSanitizer.sanitize(#{any()}))")
                            .contextSensitive()
                            .imports("java.io.File")
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(),
                                   m.getArguments().get(0));
                }
                
                // Handle Runtime.exec (command injection)
                if (m.getSelect() != null && 
                    TypeUtils.isOfClassType(m.getSelect().getType(), "java.lang.Runtime") &&
                    "exec".equals(m.getSimpleName()) && 
                    m.getArguments().size() >= 1) {
                    
                    // Create the command sanitizer utility
                    createCommandSanitizerClass();
                    
                    // Add import for our sanitizer
                    maybeAddImport("com.example.security.CommandSanitizer");
                    
                    if (m.getArguments().size() == 1) {
                        return JavaTemplate.builder("exec(CommandSanitizer.sanitize(#{any()}))")
                                .contextSensitive()
                                .build()
                                .apply(updateCursor(m), m.getCoordinates().replace(),
                                      m.getArguments().get(0));
                    }
                }
                
                return m;
            }
            
            /**
             * Create the SQL sanitizer utility class if it doesn't exist
             */
            private void createSqlSanitizerClass() {
                // This would need to be implemented to create the actual SqlSanitizer class
                // For this example, we'll just assume it exists or will be created by the recipe below
            }
            
            /**
             * Create the HTML escaper utility class if it doesn't exist
             */
            private void createHtmlEscaperClass() {
                // This would need to be implemented to create the actual HtmlEscaper class
                // For this example, we'll just assume it exists or will be created by another recipe
            }
            
            /**
             * Create the path sanitizer utility class if it doesn't exist
             */
            private void createPathSanitizerClass() {
                // This would need to be implemented to create the actual PathSanitizer class
                // For this example, we'll just assume it exists or will be created by another recipe
            }
            
            /**
             * Create the command sanitizer utility class if it doesn't exist
             */
            private void createCommandSanitizerClass() {
                // This would need to be implemented to create the actual CommandSanitizer class
                // For this example, we'll just assume it exists or will be created by another recipe
            }
        };
    }
    
    /**
     * This recipe will also create a utility class for SQL sanitization.
     */
    public static class CreateSqlSanitizerRecipe extends Recipe {
        @Override
        public String getDisplayName() {
            return "Create SQL Sanitizer";
        }
        
        @Override
        protected JavaIsoVisitor<ExecutionContext> getVisitor() {
            return new JavaIsoVisitor<ExecutionContext>() {
                @Override
                public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                    // Only create the class once
                    if (!cu.getSourcePath().toString().endsWith("SqlSanitizer.java")) {
                        return cu;
                    }
                    
                    return JavaTemplate.builder(
                            "package com.example.security;\n\n" +
                            "import java.util.regex.Pattern;\n\n" +
                            "/**\n" +
                            " * SQL sanitization utility to prevent SQL injection attacks.\n" +
                            " */\n" +
                            "public final class SqlSanitizer {\n" +
                            "    private static final Pattern SQL_INJECTION_PATTERN = \n" +
                            "            Pattern.compile(\"(?i)('\\\\s*or\\\\s*'\\\\s*=\\\\s*')|('\\\\s*or\\\\s*1\\\\s*=\\\\s*1)|" +
                            "(;\\\\s*drop\\\\s+table)|(;\\\\s*delete\\\\s+from)|(--\\\\s*$)|(\\\\bUNION\\\\b.*\\\\bSELECT\\\\b)|(\\\\bSELECT\\\\b.*\\\\bFROM\\\\b.*information_schema)\");\n\n" +
                            "    private SqlSanitizer() {\n" +
                            "        // Utility class\n" +
                            "    }\n\n" +
                            "    /**\n" +
                            "     * Sanitizes a SQL query to prevent SQL injection attacks.\n" +
                            "     *\n" +
                            "     * @param query The SQL query to sanitize\n" +
                            "     * @return The sanitized query\n" +
                            "     */\n" +
                            "    public static String clean(String query) {\n" +
                            "        if (query == null) {\n" +
                            "            return null;\n" +
                            "        }\n" +
                            "        return SQL_INJECTION_PATTERN.matcher(query).replaceAll(\"\");\n" +
                            "    }\n" +
                            "}")
                            .build()
                            .apply(getCursor(), cu.getCoordinates().replace());
                }
            };
        }
    }
    
    /**
     * Recipe to create an HTML Escaper utility class.
     */
    public static class CreateHtmlEscaperRecipe extends Recipe {
        @Override
        public String getDisplayName() {
            return "Create HTML Escaper";
        }
        
        @Override
        protected JavaIsoVisitor<ExecutionContext> getVisitor() {
            return new JavaIsoVisitor<ExecutionContext>() {
                @Override
                public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                    // Only create the class once
                    if (!cu.getSourcePath().toString().endsWith("HtmlEscaper.java")) {
                        return cu;
                    }
                    
                    return JavaTemplate.builder(
                            "package com.example.security;\n\n" +
                            "/**\n" +
                            " * HTML escaping utility to prevent XSS attacks.\n" +
                            " */\n" +
                            "public final class HtmlEscaper {\n" +
                            "    private HtmlEscaper() {\n" +
                            "        // Utility class\n" +
                            "    }\n\n" +
                            "    /**\n" +
                            "     * Escapes HTML special characters to prevent XSS attacks.\n" +
                            "     *\n" +
                            "     * @param input The input string to escape\n" +
                            "     * @return The escaped string\n" +
                            "     */\n" +
                            "    public static String escape(String input) {\n" +
                            "        if (input == null) {\n" +
                            "            return null;\n" +
                            "        }\n" +
                            "        return input\n" +
                            "            .replace(\"&\", \"&amp;\")\n" +
                            "            .replace(\"<\", \"&lt;\")\n" +
                            "            .replace(\">\", \"&gt;\")\n" +
                            "            .replace(\"\\\"\", \"&quot;\")\n" +
                            "            .replace(\"'\", \"&#39;\")\n" +
                            "            .replace(\"/\", \"&#x2F;\")\n" +
                            "            .replace(\"`\", \"&#x60;\");\n" +
                            "    }\n\n" +
                            "    /**\n" +
                            "     * Escapes an array of strings, applying HTML escaping to each non-null element.\n" +
                            "     *\n" +
                            "     * @param inputs The array of strings to escape\n" +
                            "     * @return The array with all elements escaped\n" +
                            "     */\n" +
                            "    public static String[] escapeArray(String[] inputs) {\n" +
                            "        if (inputs == null) {\n" +
                            "            return null;\n" +
                            "        }\n" +
                            "        String[] result = new String[inputs.length];\n" +
                            "        for (int i = 0; i < inputs.length; i++) {\n" +
                            "            result[i] = escape(inputs[i]);\n" +
                            "        }\n" +
                            "        return result;\n" +
                            "    }\n" +
                            "}")
                            .build()
                            .apply(getCursor(), cu.getCoordinates().replace());
                }
            };
        }
    }
} 