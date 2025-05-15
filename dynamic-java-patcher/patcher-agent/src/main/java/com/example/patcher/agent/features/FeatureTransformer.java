package com.example.patcher.agent.features;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Main transformer class that applies all SpeedDoctor features.
 */
public class FeatureTransformer {
    private static final Logger LOGGER = Logger.getLogger(FeatureTransformer.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    // Default security patterns if config file is not available
    private static final Map<String, String> DEFAULT_SECURITY_PATTERNS = Map.of(
        "SQL_INJECTION", "(?i)('\\s*or\\s*'\\s*=\\s*')|('\\s*or\\s*1\\s*=\\s*1)|(;\\s*drop\\s+table)|(;\\s*delete\\s+from)|(--\\s*$)|(\\bUNION\\b.*\\bSELECT\\b)|(\\bSELECT\\b.*\\bFROM\\b.*information_schema)",
        "XSS", "<script>|<\\/script>|javascript:|onerror=|onclick=|onload=|onmouseover=|onfocus=|onblur=|onkeydown=|onsubmit=|ondblclick=|data:text\\/html"
    );
    
    // Storage for deprecation mappings loaded from config file
    private static final Map<String, Map<String, String>> DEPRECATION_MAPPINGS = new HashMap<>();

    /**
     * Install all transformers on the given instrumentation instance.
     * 
     * @param inst Instrumentation instance
     * @param profilerPackages List of package patterns to profile
     * @param deprecationConfigPath Path to the deprecation mappings configuration file
     * @param securityPatternsPath Path to the security patterns configuration file
     */
    public static void install(Instrumentation inst, List<String> profilerPackages, 
                              String deprecationConfigPath, String securityPatternsPath) {
        LOGGER.info("Installing SpeedDoctor features");
        
        // Load configuration files
        loadDeprecationMappings(deprecationConfigPath);
        Map<String, String> securityPatterns = loadSecurityPatterns(securityPatternsPath);
        
        // Create a base agent builder
        AgentBuilder builder = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withTransformationsOnly());
        
        // Apply all features
        builder = installProfiler(builder, profilerPackages);
        builder = installDeprecationRescue(builder);
        builder = installSecurityPatches(builder, securityPatterns);
        
        // Install the combined transformer
        builder.installOn(inst);
        
        LOGGER.info("SpeedDoctor features successfully installed");
    }
    
    /**
     * Load deprecation mappings from the configuration file.
     * Format: { "className": { "methodName": "targetClass#targetMethod", ... }, ... }
     */
    private static void loadDeprecationMappings(String configPath) {
        try {
            Path path = Paths.get(configPath);
            if (Files.exists(path)) {
                Map<String, Map<String, String>> mappings = OBJECT_MAPPER.readValue(
                        path.toFile(), 
                        OBJECT_MAPPER.getTypeFactory().constructMapType(
                                HashMap.class, 
                                String.class,
                                OBJECT_MAPPER.getTypeFactory().constructMapType(
                                        HashMap.class, String.class, String.class)));
                
                DEPRECATION_MAPPINGS.putAll(mappings);
                LOGGER.info("Loaded " + DEPRECATION_MAPPINGS.size() + " deprecation mappings from " + path);
            } else {
                LOGGER.info("Deprecation mappings file not found at " + path + ", using defaults");
                // Add default mappings
                Map<String, String> mathUtilMappings = new HashMap<>();
                mathUtilMappings.put("sum", "java.lang.Math#addExact");
                
                Map<String, String> fileUtilMappings = new HashMap<>();
                fileUtilMappings.put("deleteFile", "java.nio.file.Files#deleteIfExists");
                
                Map<String, String> webUtilMappings = new HashMap<>();
                webUtilMappings.put("encodeUrl", "java.net.URLEncoder#encode");
                
                DEPRECATION_MAPPINGS.put("legacy.MathUtil", mathUtilMappings);
                DEPRECATION_MAPPINGS.put("legacy.FileUtils", fileUtilMappings);
                DEPRECATION_MAPPINGS.put("legacy.WebUtils", webUtilMappings);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load deprecation mappings: " + e.getMessage(), e);
            // Fall back to defaults in the catch block
        }
    }
    
    /**
     * Load security patterns from the configuration file.
     * Format: { "PATTERN_NAME": "regex pattern", ... }
     */
    private static Map<String, String> loadSecurityPatterns(String configPath) {
        try {
            Path path = Paths.get(configPath);
            if (Files.exists(path)) {
                Map<String, String> patterns = OBJECT_MAPPER.readValue(
                        path.toFile(), 
                        OBJECT_MAPPER.getTypeFactory().constructMapType(
                                HashMap.class, String.class, String.class));
                
                LOGGER.info("Loaded " + patterns.size() + " security patterns from " + path);
                return patterns;
            } else {
                LOGGER.info("Security patterns file not found at " + path + ", using defaults");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load security patterns: " + e.getMessage(), e);
        }
        
        // Return default patterns if loading failed
        return DEFAULT_SECURITY_PATTERNS;
    }
    
    /**
     * Install the real-time lightweight profiler.
     */
    private static AgentBuilder installProfiler(AgentBuilder builder, List<String> packages) {
        LOGGER.info("Installing Real-Time Lightweight Profiler for packages: " + String.join(", ", packages));
        
        // Create a directory for profiler output
        createOutputDirectory("profiler-data");
        
        // Build the package matcher dynamically from the list of packages
        ElementMatcher.Junction<TypeDescription> typeMatcher = null;
        
        for (String packageName : packages) {
            if (typeMatcher == null) {
                typeMatcher = nameStartsWith(packageName);
            } else {
                typeMatcher = typeMatcher.or(nameStartsWith(packageName));
            }
        }
        
        // If no packages were specified, use a sensible default
        if (typeMatcher == null) {
            typeMatcher = nameStartsWith("com.example");
        }
        
        // Method matcher to exclude trivial methods and focus on business logic
        ElementMatcher<MethodDescription> methodMatcher = not(isConstructor())
                .and(not(isStatic().and(nameStartsWith("main"))))
                .and(not(nameStartsWith("get").or(nameStartsWith("set"))))
                .and(not(nameContains("toString").or(nameContains("equals").or(nameContains("hashCode")))));
        
        return builder.type(typeMatcher)
                .transform((builder1, typeDescription, classLoader, module, protectionDomain) ->
                        builder1.method(methodMatcher)
                                .intercept(Advice.to(ProfilerAdvice.class)));
    }
    
    /**
     * Install the deprecation rescue transformers.
     */
    private static AgentBuilder installDeprecationRescue(AgentBuilder builder) {
        LOGGER.info("Installing Instant Deprecation Rescue with " + DEPRECATION_MAPPINGS.size() + " class mappings");
        
        // Process each class mapping
        for (Map.Entry<String, Map<String, String>> classEntry : DEPRECATION_MAPPINGS.entrySet()) {
            String sourceClass = classEntry.getKey();
            Map<String, String> methodMappings = classEntry.getValue();
            
            for (Map.Entry<String, String> methodEntry : methodMappings.entrySet()) {
                String sourceMethod = methodEntry.getKey();
                String targetMapping = methodEntry.getValue();
                
                String[] targetParts = targetMapping.split("#");
                if (targetParts.length != 2) {
                    LOGGER.warning("Invalid deprecation mapping format: " + targetMapping);
                    continue;
                }
                
                String targetClass = targetParts[0];
                String targetMethod = targetParts[1];
                
                LOGGER.info("Adding deprecation rescue: " + sourceClass + "." + sourceMethod + " -> " 
                        + targetClass + "." + targetMethod);
                
                // Determine which delegation method to use based on the target method
                String delegationMethod;
                if (targetMapping.equals("java.lang.Math#addExact")) {
                    delegationMethod = "legacySumShim";
                } else if (targetMapping.equals("java.nio.file.Files#deleteIfExists")) {
                    delegationMethod = "legacyFileDeleteShim";
                } else if (targetMapping.equals("java.net.URLEncoder#encode")) {
                    delegationMethod = "legacyUrlEncodeShim";
                } else {
                    // For custom mappings, we'd need a more sophisticated approach
                    LOGGER.warning("Unsupported target method, skipping: " + targetMapping);
                    continue;
                }
                
                // Add the transformer
                builder = builder.type(named(sourceClass))
                        .transform((builder1, typeDescription, classLoader, module, protectionDomain) ->
                                builder1.method(named(sourceMethod))
                                        .intercept(MethodDelegation.to(DeprecationRescueAdvice.class)
                                                .filter(named(delegationMethod))));
            }
        }
        
        return builder;
    }
    
    /**
     * Install security patch transformers.
     */
    private static AgentBuilder installSecurityPatches(AgentBuilder builder, Map<String, String> securityPatterns) {
        LOGGER.info("Installing Zero-Downtime Security Patches with " + securityPatterns.size() + " patterns");
        
        // Update the SecurityPatchAdvice with the loaded patterns
        String sqlInjectionPattern = securityPatterns.getOrDefault("SQL_INJECTION", 
                DEFAULT_SECURITY_PATTERNS.get("SQL_INJECTION"));
        String xssPattern = securityPatterns.getOrDefault("XSS", 
                DEFAULT_SECURITY_PATTERNS.get("XSS"));
        
        SecurityPatchAdvice.setSqlInjectionPattern(Pattern.compile(sqlInjectionPattern));
        SecurityPatchAdvice.setXssPattern(Pattern.compile(xssPattern));
        
        // SQL injection protection
        builder = builder.type(hasSuperType(named("java.sql.Statement")))
                .transform((builder1, typeDescription, classLoader, module, protectionDomain) ->
                        builder1.method(named("executeQuery").and(takesArguments(String.class)))
                                .intercept(Advice.to(SecurityPatchAdvice.class)));
        
        // Add protection for executeUpdate as well
        builder = builder.type(hasSuperType(named("java.sql.Statement")))
                .transform((builder1, typeDescription, classLoader, module, protectionDomain) ->
                        builder1.method(named("executeUpdate").and(takesArguments(String.class)))
                                .intercept(Advice.to(SecurityPatchAdvice.class)));
        
        // XSS protection
        builder = builder.type(hasSuperType(named("javax.servlet.http.HttpServletRequest")))
                .transform((builder1, typeDescription, classLoader, module, protectionDomain) ->
                        builder1.method(named("getParameter").and(takesArguments(String.class)))
                                .intercept(Advice.to(SecurityPatchAdvice.HttpSanitizer.class)));
        
        // Add protection for getParameterValues
        builder = builder.type(hasSuperType(named("javax.servlet.http.HttpServletRequest")))
                .transform((builder1, typeDescription, classLoader, module, protectionDomain) ->
                        builder1.method(named("getParameterValues").and(takesArguments(String.class)))
                                .intercept(Advice.to(SecurityPatchAdvice.HttpArraySanitizer.class)));
        
        return builder;
    }
    
    /**
     * Create an output directory if it doesn't exist.
     * 
     * @param dirName The directory name
     */
    private static void createOutputDirectory(String dirName) {
        try {
            Path path = Paths.get(dirName);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                LOGGER.info("Created directory: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to create directory: " + dirName, e);
        }
    }
    
    /**
     * Save hotspot data from the profiler to a CSV file for OpenRewrite recipes.
     */
    public static void saveHotspotData() {
        try {
            Path dir = Paths.get("profiler-data");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            
            Path file = dir.resolve("hotspots.csv");
            try (FileWriter writer = new FileWriter(file.toFile())) {
                writer.write(ProfilerAdvice.getHotspotReport());
            }
            
            LOGGER.info("Saved hotspot data to: " + file.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save hotspot data", e);
        }
    }
} 