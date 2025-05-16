import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Demo that programmatically attaches the SpeedDoctor agent to the JVM
 * and then runs the demo workload.
 */
public class ProgrammaticAgentDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("===== SpeedDoctor Programmatic Agent Attachment Demo =====");
        
        // Path to the agent JAR
        String agentJarPath = "../patcher-agent/target/patcher-agent-1.0-SNAPSHOT.jar";
        
        // Verify the agent JAR exists
        File agentJar = new File(agentJarPath);
        if (!agentJar.exists()) {
            System.err.println("Agent JAR not found at: " + agentJar.getAbsolutePath());
            System.exit(1);
        }
        
        System.out.println("Found agent JAR at: " + agentJar.getAbsolutePath());
        
        // Load the agent JAR into our classloader
        try {
            // Create a URLClassLoader to load the agent JAR
            URLClassLoader classLoader = new URLClassLoader(
                new URL[] { agentJar.toURI().toURL() },
                ProgrammaticAgentDemo.class.getClassLoader()
            );
            
            // Load the PatcherAgent class
            Class<?> agentClass = classLoader.loadClass("com.example.patcher.agent.PatcherAgent");
            
            // Call the attach method to initialize the agent
            Method attachMethod = agentClass.getMethod("attach");
            attachMethod.invoke(null);
            
            System.out.println("Successfully attached the SpeedDoctor agent programmatically!");
            
            // Set the feature flags through system properties
            System.setProperty("speeddoctor.profiler", "true");
            System.setProperty("speeddoctor.deprecationrescue", "true");
            System.setProperty("speeddoctor.securitypatches", "true");
            System.setProperty("speeddoctor.profiler.packages", "SpeedDoctorDemo");
            
            // Run the demo workload
            SpeedDoctorDemo.main(new String[0]);
            
        } catch (Exception e) {
            System.err.println("Failed to attach agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 