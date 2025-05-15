package com.example.patcher.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Recipe to find repeated Pattern.compile() calls with the same pattern and replace them with
 * cached static final Pattern fields.
 */
public class CacheCompiledPattern extends Recipe {

    @Override
    public String getDisplayName() {
        return "Cache Compiled Pattern Objects";
    }

    @Override
    public String getDescription() {
        return "Cache compiled Pattern objects instead of repeatedly calling Pattern.compile() with the same string.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CacheCompiledPatternVisitor();
    }

    private static class CacheCompiledPatternVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final MethodMatcher patternCompileMatcher = new MethodMatcher("java.util.regex.Pattern compile(java.lang.String)");
        private final MethodMatcher patternCompileWithFlagsMatcher = new MethodMatcher("java.util.regex.Pattern compile(java.lang.String, int)");
        private final Map<String, String> patternFieldMap = new HashMap<>();

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            // First, collect all pattern.compile calls to determine which ones to cache
            Map<PatternInfo, List<J.MethodInvocation>> patternsToCache = new HashMap<>();
            PatternCollector collector = new PatternCollector();
            classDecl.accept(collector, ctx);
            
            // Group method invocations by pattern
            for (J.MethodInvocation method : collector.getPatternCompileCalls()) {
                if (method.getArguments().size() >= 1 && method.getArguments().get(0) instanceof J.Literal) {
                    J.Literal patternLiteral = (J.Literal) method.getArguments().get(0);
                    if (patternLiteral.getValue() instanceof String) {
                        String pattern = (String) patternLiteral.getValue();
                        String flags = null;
                        
                        // Check if there are flags
                        if (method.getArguments().size() >= 2) {
                            if (method.getArguments().get(1) instanceof J.Literal) {
                                J.Literal flagsLiteral = (J.Literal) method.getArguments().get(1);
                                if (flagsLiteral.getValue() instanceof Integer) {
                                    flags = flagsLiteral.getValue().toString();
                                }
                            }
                        }
                        
                        PatternInfo patternInfo = new PatternInfo(pattern, flags);
                        patternsToCache.computeIfAbsent(patternInfo, k -> new ArrayList<>()).add(method);
                    }
                }
            }
            
            // Now proceed only with patterns that are used more than once
            Map<PatternInfo, String> fieldNames = new HashMap<>();
            J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);
            
            for (Map.Entry<PatternInfo, List<J.MethodInvocation>> entry : patternsToCache.entrySet()) {
                if (entry.getValue().size() > 1) {
                    PatternInfo patternInfo = entry.getKey();
                    String fieldName = "PATTERN_" + sanitizeForFieldName(patternInfo.patternLiteral) + "_" + 
                                       UUID.randomUUID().toString().substring(0, 8);
                    fieldNames.put(patternInfo, fieldName);
                    
                    // Add static final Pattern field to the class
                    JavaTemplate patternFieldTemplate;
                    if (patternInfo.flags == null) {
                        patternFieldTemplate = JavaTemplate.builder(
                                "private static final java.util.regex.Pattern " + fieldName + 
                                " = java.util.regex.Pattern.compile(\"" + escapeJavaString(patternInfo.patternLiteral) + "\");")
                                .contextSensitive()
                                .build();
                    } else {
                        patternFieldTemplate = JavaTemplate.builder(
                                "private static final java.util.regex.Pattern " + fieldName + 
                                " = java.util.regex.Pattern.compile(\"" + escapeJavaString(patternInfo.patternLiteral) + "\", " + 
                                patternInfo.flags + ");")
                                .contextSensitive()
                                .build();
                    }
                    
                    cd = patternFieldTemplate.apply(
                            getCursor(), 
                            cd.getBody().getCoordinates().firstStatement());
                    
                    // Store mapping to replace method calls
                    patternFieldMap.put(patternInfo.toString(), fieldName);
                }
            }
            
            return cd;
        }
        
        private String sanitizeForFieldName(String input) {
            String sanitized = input.replaceAll("[^a-zA-Z0-9_]", "_").replaceAll("_+", "_");
            if (sanitized.length() > 30) {
                sanitized = sanitized.substring(0, 30);
            }
            return sanitized;
        }
        
        private String escapeJavaString(String s) {
            return s.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
        }
        
        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
            
            if ((patternCompileMatcher.matches(m) || patternCompileWithFlagsMatcher.matches(m)) &&
                m.getArguments().size() >= 1 && 
                m.getArguments().get(0) instanceof J.Literal) {
                
                J.Literal patternLiteral = (J.Literal) m.getArguments().get(0);
                if (patternLiteral.getValue() instanceof String) {
                    String pattern = (String) patternLiteral.getValue();
                    String flags = null;
                    
                    // Check if there are flags
                    if (m.getArguments().size() >= 2) {
                        if (m.getArguments().get(1) instanceof J.Literal) {
                            J.Literal flagsLiteral = (J.Literal) m.getArguments().get(1);
                            if (flagsLiteral.getValue() instanceof Integer) {
                                flags = flagsLiteral.getValue().toString();
                            }
                        }
                    }
                    
                    PatternInfo patternInfo = new PatternInfo(pattern, flags);
                    String fieldName = patternFieldMap.get(patternInfo.toString());
                    
                    if (fieldName != null) {
                        // Replace Pattern.compile() call with field reference
                        return JavaTemplate.builder(fieldName)
                                .contextSensitive()
                                .build()
                                .apply(getCursor(), m.getCoordinates().replace());
                    }
                }
            }
            
            return m;
        }
    }
    
    /**
     * Helper class to store information about a pattern to cache.
     */
    private static class PatternInfo {
        final String patternLiteral;
        final String flags;
        
        PatternInfo(String patternLiteral, String flags) {
            this.patternLiteral = patternLiteral;
            this.flags = flags;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatternInfo that = (PatternInfo) o;
            return patternLiteral.equals(that.patternLiteral) && 
                   (flags == null ? that.flags == null : flags.equals(that.flags));
        }
        
        @Override
        public int hashCode() {
            int result = patternLiteral.hashCode();
            result = 31 * result + (flags != null ? flags.hashCode() : 0);
            return result;
        }
        
        @Override
        public String toString() {
            return flags == null 
                ? patternLiteral 
                : patternLiteral + "#" + flags;
        }
    }
    
    /**
     * Visitor to collect Pattern.compile() calls.
     */
    private static class PatternCollector extends JavaIsoVisitor<ExecutionContext> {
        private final MethodMatcher patternCompileMatcher = new MethodMatcher("java.util.regex.Pattern compile(java.lang.String)");
        private final MethodMatcher patternCompileWithFlagsMatcher = new MethodMatcher("java.util.regex.Pattern compile(java.lang.String, int)");
        private final List<J.MethodInvocation> patternCompileCalls = new ArrayList<>();
        
        List<J.MethodInvocation> getPatternCompileCalls() {
            return patternCompileCalls;
        }
        
        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
            
            if (patternCompileMatcher.matches(m) || patternCompileWithFlagsMatcher.matches(m)) {
                patternCompileCalls.add(m);
            }
            
            return m;
        }
    }
} 