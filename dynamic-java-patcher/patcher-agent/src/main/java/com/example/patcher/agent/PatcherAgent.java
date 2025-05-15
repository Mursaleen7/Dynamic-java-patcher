package com.example.patcher.agent;

import com.example.patcher.agent.features.FeatureTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Java agent that can dynamically patch classes at runtime using OpenRewrite-generated patches.
 * Now enhanced with SpeedDoctor features.
 */
public class PatcherAgent {
    private static final Logger LOGGER = Logger.getLogger(PatcherAgent.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    // Patch endpoint configuration (could be made configurable via system properties)
    private static final String PATCH_ENDPOINT = System.getProperty("patcher.endpoint", "http://localhost:8080/patches");
    private static final long POLLING_INTERVAL_MINUTES = Long.parseLong(System.getProperty("patcher.polling.minutes", "5"));
    
    // SpeedDoctor feature flags
    private static final boolean ENABLE_PROFILER = Boolean.parseBoolean(System.getProperty("speeddoctor.profiler", "true"));
    private static final boolean ENABLE_DEPRECATION_RESCUE = Boolean.parseBoolean(System.getProperty("speeddoctor.deprecationrescue", "true"));
    private static final boolean ENABLE_SECURITY_PATCHES = Boolean.parseBoolean(System.getProperty("speeddoctor.securitypatches", "true"));
    
    // SpeedDoctor feature configurations
    private static final String PROFILER_PACKAGES = System.getProperty("speeddoctor.profiler.packages", 
            "com.example,org.springframework,com.company");
    private static final String DEPRECATION_CONFIG_PATH = System.getProperty("speeddoctor.deprecation.config", 
            "config/deprecation-mappings.json");
    private static final String SECURITY_PATTERNS_PATH = System.getProperty("speeddoctor.security.patterns", 
            "config/security-patterns.json");
    
    // Keep track of applied patches to avoid reapplying
    private static final Map<String, Long> APPLIED_PATCHES = new ConcurrentHashMap<>();
    
    private static Instrumentation instrumentation;
    private static volatile boolean isRunning = false;
    private static ScheduledExecutorService scheduler;

    /**
     * Premain method called when the agent is loaded at JVM startup.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        LOGGER.info("Patcher agent loaded at JVM startup");
        instrumentation = inst;
        
        // Initialize SpeedDoctor features
        initializeSpeedDoctor(inst);
        
        // Start the patch poller
        startPatchPoller();
    }
    
    /**
     * Agent method called when the agent is attached to a running JVM.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        LOGGER.info("Patcher agent attached to running JVM");
        instrumentation = inst;
        
        // Initialize SpeedDoctor features
        initializeSpeedDoctor(inst);
        
        // Start the patch poller
        startPatchPoller();
    }
    
    /**
     * Initialize SpeedDoctor features based on configuration.
     */
    private static void initializeSpeedDoctor(Instrumentation inst) {
        if (ENABLE_PROFILER || ENABLE_DEPRECATION_RESCUE || ENABLE_SECURITY_PATCHES) {
            LOGGER.info("Initializing SpeedDoctor features");
            
            // Parse profiler package patterns
            List<String> profilerPackages = Arrays.stream(PROFILER_PACKAGES.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            // Install transformers with configuration
            FeatureTransformer.install(inst, profilerPackages, DEPRECATION_CONFIG_PATH, SECURITY_PATTERNS_PATH);
            
            // Set up a shutdown hook to save profiler data
            if (ENABLE_PROFILER) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    LOGGER.info("Saving profiler data before shutdown");
                    FeatureTransformer.saveHotspotData();
                }));
            }
        }
    }
    
    /**
     * Programmatically attach the agent to the current JVM if not already attached.
     */
    public static synchronized void attach() {
        if (instrumentation == null) {
            try {
                instrumentation = ByteBuddyAgent.install();
                LOGGER.info("Patcher agent programmatically attached");
                
                // Initialize SpeedDoctor features
                initializeSpeedDoctor(instrumentation);
                
                // Start the patch poller
                startPatchPoller();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to attach agent programmatically", e);
            }
        }
    }
    
    /**
     * Start the poller that checks for new patches periodically.
     */
    private static synchronized void startPatchPoller() {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            PatcherAgent::checkForPatches,
            0,
            POLLING_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
        
        LOGGER.info("Patch poller started, checking every " + POLLING_INTERVAL_MINUTES + " minutes");
    }
    
    /**
     * Stop the patch poller.
     */
    public static synchronized void stopPatchPoller() {
        if (!isRunning) {
            return;
        }
        
        scheduler.shutdown();
        isRunning = false;
        LOGGER.info("Patch poller stopped");
    }
    
    /**
     * Check for new patches from the configured endpoint.
     */
    private static void checkForPatches() {
        try {
            LOGGER.info("Checking for patches at " + PATCH_ENDPOINT);
            
            // Check for JSON patch manifest file
            PatchManifest manifest;
            
            // URI-based approach (HTTP endpoint)
            if (PATCH_ENDPOINT.startsWith("http")) {
                manifest = fetchPatchManifestFromHttp();
            } 
            // File system approach
            else {
                manifest = fetchPatchManifestFromFileSystem();
            }
            
            if (manifest == null) {
                LOGGER.info("No patch manifest found or no changes detected");
                return;
            }
            
            // Skip if we've already applied this version
            if (APPLIED_PATCHES.containsKey(manifest.getVersion()) && 
                APPLIED_PATCHES.get(manifest.getVersion()) >= manifest.getTimestamp()) {
                LOGGER.info("Patch version " + manifest.getVersion() + " already applied");
                return;
            }
            
            // Apply the patches
            applyPatches(manifest);
            
            // Mark as applied
            APPLIED_PATCHES.put(manifest.getVersion(), manifest.getTimestamp());
            LOGGER.info("Successfully applied patch version " + manifest.getVersion());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking for patches", e);
        }
    }
    
    private static PatchManifest fetchPatchManifestFromHttp() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(PATCH_ENDPOINT + "/manifest.json"))
            .GET()
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return OBJECT_MAPPER.readValue(response.body(), PatchManifest.class);
        } else if (response.statusCode() == 404) {
            LOGGER.info("No patch manifest found at " + PATCH_ENDPOINT);
            return null;
        } else {
            LOGGER.warning("Failed to fetch patch manifest: HTTP " + response.statusCode());
            return null;
        }
    }
    
    private static PatchManifest fetchPatchManifestFromFileSystem() throws IOException {
        Path manifestPath = Paths.get(PATCH_ENDPOINT, "manifest.json");
        if (!Files.exists(manifestPath)) {
            LOGGER.info("No patch manifest found at " + manifestPath);
            return null;
        }
        
        return OBJECT_MAPPER.readValue(manifestPath.toFile(), PatchManifest.class);
    }
    
    /**
     * Apply the patches from the manifest.
     */
    private static void applyPatches(PatchManifest manifest) throws Exception {
        if (manifest.getPatches() == null || manifest.getPatches().isEmpty()) {
            LOGGER.info("No patches found in manifest");
            return;
        }
        
        for (PatchEntry patch : manifest.getPatches()) {
            try {
                applyPatch(patch, manifest.getVersion());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to apply patch for class " + patch.getClassName(), e);
            }
        }
    }
    
    /**
     * Apply an individual patch.
     */
    private static void applyPatch(PatchEntry patch, String version) throws Exception {
        String className = patch.getClassName();
        byte[] patchedBytes;
        
        // Get the patched bytecode
        if (PATCH_ENDPOINT.startsWith("http")) {
            patchedBytes = fetchPatchedBytesFromHttp(patch.getPath(), version);
        } else {
            patchedBytes = fetchPatchedBytesFromFileSystem(patch.getPath());
        }
        
        if (patchedBytes == null || patchedBytes.length == 0) {
            LOGGER.warning("No bytecode found for patch " + patch.getPath());
            return;
        }
        
        // Find the class to patch
        Class<?> clazz = loadClass(className);
        if (clazz == null) {
            LOGGER.warning("Class not found: " + className);
            return;
        }
        
        // Create the class definition
        ClassDefinition definition = new ClassDefinition(clazz, patchedBytes);
        
        // Apply the patch
        LOGGER.info("Applying patch for " + className);
        instrumentation.redefineClasses(definition);
    }
    
    private static byte[] fetchPatchedBytesFromHttp(String path, String version) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(PATCH_ENDPOINT + "/" + version + "/" + path))
            .GET()
            .build();
            
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            LOGGER.warning("Failed to fetch patch bytes: HTTP " + response.statusCode());
            return null;
        }
    }
    
    private static byte[] fetchPatchedBytesFromFileSystem(String path) throws IOException {
        Path patchPath = Paths.get(PATCH_ENDPOINT, path);
        if (!Files.exists(patchPath)) {
            LOGGER.warning("Patch file not found: " + patchPath);
            return null;
        }
        
        return Files.readAllBytes(patchPath);
    }
    
    /**
     * Load a class by name.
     */
    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e2) {
                return null;
            }
        }
    }
} 