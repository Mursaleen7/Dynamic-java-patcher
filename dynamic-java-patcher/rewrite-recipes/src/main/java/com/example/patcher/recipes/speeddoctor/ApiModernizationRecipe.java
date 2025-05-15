package com.example.patcher.recipes.speeddoctor;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Recipe that modernizes API calls by replacing deprecated methods with their newer counterparts.
 */
public class ApiModernizationRecipe extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "API Modernization";
    }
    
    @Override
    public String getDescription() {
        return "Automatically updates deprecated API calls to their modern equivalents.";
    }
    
    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            
            /**
             * Replace legacy.MathUtil.sum(a, b) with java.lang.Math.addExact(a, b)
             */
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
                
                if (m.getSelect() != null && TypeUtils.isOfClassType(m.getSelect().getType(), "legacy.MathUtil") 
                        && "sum".equals(m.getSimpleName()) && m.getArguments().size() == 2) {
                    
                    maybeAddImport("java.lang.Math");
                    
                    return JavaTemplate.builder("Math.addExact(#{any()}, #{any()})")
                            .contextSensitive()
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(), 
                                   m.getArguments().get(0), m.getArguments().get(1));
                }
                
                return m;
            }
            
            /**
             * Replace legacy.FileUtils.deleteFile(path) with 
             * java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path))
             */
            @Override
            public J.MethodInvocation visitMethodInvocation2(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
                
                if (m.getSelect() != null && TypeUtils.isOfClassType(m.getSelect().getType(), "legacy.FileUtils") 
                        && "deleteFile".equals(m.getSimpleName()) && m.getArguments().size() == 1) {
                    
                    maybeAddImport("java.nio.file.Files");
                    maybeAddImport("java.nio.file.Paths");
                    
                    return JavaTemplate.builder("Files.deleteIfExists(Paths.get(#{any()}))")
                            .contextSensitive()
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(), m.getArguments().get(0));
                }
                
                return m;
            }
            
            /**
             * Replace legacy.WebUtils.encodeUrl(str) with 
             * java.net.URLEncoder.encode(str, java.nio.charset.StandardCharsets.UTF_8.name())
             */
            @Override
            public J.MethodInvocation visitMethodInvocation3(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
                
                if (m.getSelect() != null && TypeUtils.isOfClassType(m.getSelect().getType(), "legacy.WebUtils") 
                        && "encodeUrl".equals(m.getSimpleName()) && m.getArguments().size() == 1) {
                    
                    maybeAddImport("java.net.URLEncoder");
                    maybeAddImport("java.nio.charset.StandardCharsets");
                    
                    return JavaTemplate.builder("URLEncoder.encode(#{any()}, StandardCharsets.UTF_8.name())")
                            .contextSensitive()
                            .build()
                            .apply(updateCursor(m), m.getCoordinates().replace(), m.getArguments().get(0));
                }
                
                return m;
            }
        };
    }
} 