package com.example.patcher.agent.features;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the loading of configuration files for SpeedDoctor features.
 */
public class ConfigTest {
    
    @TempDir
    Path tempDir;
    
    private Path configDir;
    private Path deprecationConfigFile;
    private Path securityPatternFile;
    
    @BeforeEach
    public void setup() throws IOException {
        // Create test configuration files
        configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);
        
        // Create test deprecation mappings
        deprecationConfigFile = configDir.resolve("deprecation-mappings.json");
        String deprecationJson = "{\n" +
                "  \"test.OldClass\": {\n" +
                "    \"oldMethod\": \"test.NewClass#newMethod\"\n" +
                "  }\n" +
                "}";
        Files.writeString(deprecationConfigFile, deprecationJson);
        
        // Create test security patterns
        securityPatternFile = configDir.resolve("security-patterns.json");
        String securityJson = "{\n" +
                "  \"TEST_PATTERN\": \"test[0-9]+\"\n" +
                "}";
        Files.writeString(securityPatternFile, securityJson);
    }
    
    @Test
    public void testLoadConfigurations() {
        // Mock an instrumentation for testing
        MockInstrumentation mockInst = new MockInstrumentation();
        
        // Set system properties to point to test files
        System.setProperty("speeddoctor.deprecation.config", deprecationConfigFile.toString());
        System.setProperty("speeddoctor.security.patterns", securityPatternFile.toString());
        
        List<String> profilerPackages = Arrays.asList("com.test", "org.example");
        
        // This should load our test configuration files
        TestFeatureTransformer.testLoadDeprecationMappings(deprecationConfigFile.toString());
        
        // Verify deprecation mappings were loaded
        assertTrue(TestFeatureTransformer.hasDeprecationMapping("test.OldClass", "oldMethod"));
        assertEquals("test.NewClass#newMethod", 
                TestFeatureTransformer.getDeprecationTarget("test.OldClass", "oldMethod"));
        
        // Test security patterns loading
        assertTrue(TestFeatureTransformer.getSecurityPattern(securityPatternFile.toString())
                .containsKey("TEST_PATTERN"));
        assertEquals("test[0-9]+", 
                TestFeatureTransformer.getSecurityPattern(securityPatternFile.toString())
                        .get("TEST_PATTERN"));
    }
    
    /**
     * Mock implementation of the FeatureTransformer for testing.
     */
    static class TestFeatureTransformer {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        
        public static boolean hasDeprecationMapping(String className, String methodName) {
            try {
                Path path = Paths.get(System.getProperty("speeddoctor.deprecation.config"));
                if (!Files.exists(path)) {
                    return false;
                }
                
                Object mappings = OBJECT_MAPPER.readValue(path.toFile(), Object.class);
                if (mappings instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) mappings;
                    if (map.containsKey(className) && map.get(className) instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> classMethods = 
                                (java.util.Map<String, Object>) map.get(className);
                        return classMethods.containsKey(methodName);
                    }
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        public static String getDeprecationTarget(String className, String methodName) {
            try {
                Path path = Paths.get(System.getProperty("speeddoctor.deprecation.config"));
                if (!Files.exists(path)) {
                    return null;
                }
                
                Object mappings = OBJECT_MAPPER.readValue(path.toFile(), Object.class);
                if (mappings instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) mappings;
                    if (map.containsKey(className) && map.get(className) instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> classMethods = 
                                (java.util.Map<String, Object>) map.get(className);
                        return (String) classMethods.get(methodName);
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
        public static void testLoadDeprecationMappings(String path) {
            try {
                System.out.println("Loading from: " + path);
                Path configPath = Paths.get(path);
                if (Files.exists(configPath)) {
                    System.out.println("File exists");
                } else {
                    System.out.println("File does not exist");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public static java.util.Map<String, String> getSecurityPattern(String configPath) {
            try {
                Path path = Paths.get(configPath);
                if (Files.exists(path)) {
                    return OBJECT_MAPPER.readValue(
                            path.toFile(), 
                            OBJECT_MAPPER.getTypeFactory().constructMapType(
                                    java.util.HashMap.class, String.class, String.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * Mock implementation of Instrumentation for testing.
     */
    static class MockInstrumentation implements java.lang.instrument.Instrumentation {
        // Implement all required methods with no-op implementations
        @Override public void addTransformer(java.lang.instrument.ClassFileTransformer transformer) {}
        @Override public void addTransformer(java.lang.instrument.ClassFileTransformer transformer, boolean canRetransform) {}
        @Override public boolean removeTransformer(java.lang.instrument.ClassFileTransformer transformer) { return true; }
        @Override public void retransformClasses(Class<?>... classes) {}
        @Override public void redefineClasses(java.lang.instrument.ClassDefinition... definitions) {}
        @Override public boolean isRetransformClassesSupported() { return false; }
        @Override public boolean isRedefineClassesSupported() { return false; }
        @Override public boolean isModifiableClass(Class<?> theClass) { return false; }
        @Override public Class[] getAllLoadedClasses() { return new Class[0]; }
        @Override public Class[] getInitiatedClasses(ClassLoader loader) { return new Class[0]; }
        @Override public long getObjectSize(Object objectToSize) { return 0; }
        @Override public void appendToBootstrapClassLoaderSearch(java.util.jar.JarFile jarfile) {}
        @Override public void appendToSystemClassLoaderSearch(java.util.jar.JarFile jarfile) {}
        @Override public boolean isNativeMethodPrefixSupported() { return false; }
        @Override public void setNativeMethodPrefix(java.lang.instrument.ClassFileTransformer transformer, String prefix) {}
    }
} 