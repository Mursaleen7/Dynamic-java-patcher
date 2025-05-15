package com.example.patcher.recipes.speeddoctor;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recipe that only operates on files that have been identified as performance hotspots by the profiler.
 * This allows targeting optimization recipes only to the files that need them most.
 */
public class HotspotRecipe extends Recipe {
    private final Set<String> hotspotClasses = new HashSet<>();
    
    /**
     * Constructor that reads hotspot data from a CSV file.
     */
    public HotspotRecipe() {
        try {
            Path hotspotFile = Paths.get("profiler-data", "hotspots.csv");
            if (Files.exists(hotspotFile)) {
                // Skip the header row
                hotspotClasses.addAll(
                    Files.lines(hotspotFile)
                        .skip(1)
                        .map(line -> {
                            // Extract class name from method signature (before the last dot)
                            String[] parts = line.split(",");
                            if (parts.length >= 1) {
                                String signature = parts[0];
                                int lastDot = signature.lastIndexOf('.');
                                if (lastDot > 0) {
                                    return signature.substring(0, lastDot);
                                }
                            }
                            return null;
                        })
                        .filter(className -> className != null)
                        .collect(Collectors.toSet())
                );
            }
        } catch (IOException e) {
            // Fall back to an empty set if we can't read the file
            System.err.println("Could not read hotspot data: " + e.getMessage());
        }
    }
    
    @Override
    public String getDisplayName() {
        return "Optimize performance hotspots";
    }
    
    @Override
    public String getDescription() {
        return "Applies optimization only to classes that were identified as performance hotspots by the profiler.";
    }
    
    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public JavaSourceFile visitJavaSourceFile(JavaSourceFile cu, ExecutionContext ctx) {
                // Skip files that are not in our hotspot list
                String className = cu.getSourcePath().toString()
                        .replace('/', '.')
                        .replace('\\', '.')
                        .replaceAll("\\.java$", "");
                
                if (hotspotClasses.isEmpty() || hotspotClasses.contains(className)) {
                    return super.visitJavaSourceFile(cu, ctx);
                }
                
                return cu;
            }
        };
    }
} 