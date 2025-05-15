package com.example.patcher.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A recipe that replaces string concatenation in loops with StringBuilder.
 * This is more efficient because it avoids creating a new String object on each concatenation.
 */
public class StringConcatenationToStringBuilder extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use StringBuilder Instead of String Concatenation";
    }

    @Override
    public String getDescription() {
        return "Replaces repeated string concatenation using the + operator inside loops with StringBuilder.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new StringConcatenationVisitor();
    }

    private static class StringConcatenationVisitor extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
            J.Block b = super.visitBlock(block, ctx);
            
            List<Statement> statements = new ArrayList<>(b.getStatements());
            boolean changed = false;
            
            for (int i = 0; i < statements.size(); i++) {
                Statement stmt = statements.get(i);
                
                // Check if this is a loop (for, while, do-while)
                if (stmt instanceof J.ForLoop) {
                    J.ForLoop forLoop = (J.ForLoop) stmt;
                    
                    // Find string concatenation variables inside the loop
                    StringConcatFinder finder = new StringConcatFinder();
                    forLoop.getBody().accept(finder, ctx);
                    
                    // Transform string concatenation in the loop if found
                    if (!finder.getStringConcats().isEmpty()) {
                        for (StringConcatInfo concatInfo : finder.getStringConcats()) {
                            J.Identifier varName = concatInfo.getVarName();
                            String sbVarName = "sb_" + varName.getSimpleName() + "_" + UUID.randomUUID().toString().substring(0, 8);
                            
                            // 1. Insert StringBuilder initialization before the loop
                            JavaTemplate sbInit = JavaTemplate.builder("StringBuilder " + sbVarName + " = new StringBuilder(" + varName.getSimpleName() + ");")
                                    .contextSensitive()
                                    .build();
                                    
                            statements.add(i, sbInit.apply(getCursor(), b.getCoordinates().firstStatement(), varName.getSimpleName()));
                            i++; // Move index since we added a statement
                            
                            // 2. Replace string concatenation inside the loop with StringBuilder.append
                            J.ForLoop updatedLoop = (J.ForLoop) new StringConcatReplacer(varName.getSimpleName(), sbVarName)
                                    .visitForLoop(forLoop, ctx);
                                    
                            statements.set(i, updatedLoop);
                            
                            // 3. Add StringBuilder to String conversion after the loop
                            JavaTemplate sbToString = JavaTemplate.builder(varName.getSimpleName() + " = " + sbVarName + ".toString();")
                                    .contextSensitive()
                                    .build();
                                    
                            statements.add(i + 1, sbToString.apply(getCursor(), b.getCoordinates().lastStatement(), varName.getSimpleName()));
                            i++; // Move index since we added a statement
                            
                            changed = true;
                        }
                    }
                }
            }
            
            if (changed) {
                return b.withStatements(statements);
            }
            
            return b;
        }
    }
    
    /**
     * Helper class to find string concatenation variables in a loop.
     */
    private static class StringConcatFinder extends JavaIsoVisitor<ExecutionContext> {
        private final List<StringConcatInfo> stringConcats = new ArrayList<>();
        
        List<StringConcatInfo> getStringConcats() {
            return stringConcats;
        }
        
        @Override
        public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext ctx) {
            J.Assignment a = super.visitAssignment(assignment, ctx);
            
            if (a.getVariable() instanceof J.Identifier &&
                a.getAssignment() instanceof J.Binary) {
                
                J.Binary binary = (J.Binary) a.getAssignment();
                J.Identifier varName = (J.Identifier) a.getVariable();
                
                // Check if this is string concatenation (String + Something)
                if (binary.getOperator() == J.Binary.Type.Addition &&
                    TypeUtils.isString(binary.getType()) &&
                    binary.getLeft() instanceof J.Identifier &&
                    varName.getSimpleName().equals(((J.Identifier) binary.getLeft()).getSimpleName())) {
                    
                    stringConcats.add(new StringConcatInfo(varName, binary));
                }
            }
            
            return a;
        }
    }
    
    /**
     * Helper class to replace string concatenation with StringBuilder.append.
     */
    private static class StringConcatReplacer extends JavaIsoVisitor<ExecutionContext> {
        private final String stringVarName;
        private final String sbVarName;
        
        StringConcatReplacer(String stringVarName, String sbVarName) {
            this.stringVarName = stringVarName;
            this.sbVarName = sbVarName;
        }
        
        @Override
        public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext ctx) {
            J.Assignment a = super.visitAssignment(assignment, ctx);
            
            if (a.getVariable() instanceof J.Identifier &&
                a.getAssignment() instanceof J.Binary) {
                
                J.Binary binary = (J.Binary) a.getAssignment();
                J.Identifier varName = (J.Identifier) a.getVariable();
                
                // Check if this is the string concatenation we want to replace
                if (binary.getOperator() == J.Binary.Type.Addition &&
                    TypeUtils.isString(binary.getType()) &&
                    varName.getSimpleName().equals(stringVarName) &&
                    binary.getLeft() instanceof J.Identifier &&
                    ((J.Identifier) binary.getLeft()).getSimpleName().equals(stringVarName)) {
                    
                    // Replace with StringBuilder.append
                    JavaTemplate appendTemplate = JavaTemplate.builder(sbVarName + ".append(#{any(java.lang.Object)})")
                            .contextSensitive()
                            .build();
                            
                    return appendTemplate.apply(getCursor(), a.getCoordinates().replace(), binary.getRight());
                }
            }
            
            return a;
        }
    }
    
    /**
     * Helper class to store information about string concatenation.
     */
    private static class StringConcatInfo {
        private final J.Identifier varName;
        private final J.Binary concatenation;
        
        StringConcatInfo(J.Identifier varName, J.Binary concatenation) {
            this.varName = varName;
            this.concatenation = concatenation;
        }
        
        J.Identifier getVarName() {
            return varName;
        }
        
        J.Binary getConcatenation() {
            return concatenation;
        }
    }
} 