package com.example.patcher.agent.features;

import net.bytebuddy.asm.Advice;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advice for measuring method execution time and identifying performance hotspots.
 */
public class ProfilerAdvice {
    
    private static final Logger LOGGER = Logger.getLogger(ProfilerAdvice.class.getName());
    private static final Map<String, AtomicLong> SLOW_METHODS = new ConcurrentHashMap<>();
    private static final long REPORTING_THRESHOLD_MS = 50; // Only report methods that take longer than 50ms
    
    /**
     * Called before the intercepted method is executed.
     */
    @Advice.OnMethodEnter
    public static long start() {
        return System.nanoTime();
    }
    
    /**
     * Called after the intercepted method is executed.
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void end(@Advice.Origin String signature, @Advice.Enter long start) {
        long durationNanos = System.nanoTime() - start;
        long durationMs = durationNanos / 1_000_000;
        
        if (durationMs > REPORTING_THRESHOLD_MS) {
            SLOW_METHODS.computeIfAbsent(signature, k -> new AtomicLong()).incrementAndGet();
            LOGGER.info("[Profiler] " + signature + " took " + durationMs + "ms");
            
            // Periodically output the top slow methods for analysis
            if (SLOW_METHODS.size() % 10 == 0) {
                logHotspots();
            }
        }
    }
    
    /**
     * Log the current hotspots to help identify performance issues.
     */
    private static void logHotspots() {
        StringBuilder sb = new StringBuilder("\n==== PERFORMANCE HOTSPOTS ====\n");
        
        SLOW_METHODS.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
            .limit(5)
            .forEach(entry -> {
                sb.append(entry.getKey()).append(": ")
                  .append(entry.getValue().get()).append(" hits\n");
            });
        
        sb.append("===============================");
        LOGGER.info(sb.toString());
    }
    
    /**
     * Get a CSV report of the detected hotspots for OpenRewrite recipes.
     */
    public static String getHotspotReport() {
        StringBuilder report = new StringBuilder("method,hits\n");
        
        SLOW_METHODS.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
            .forEach(entry -> {
                report.append(entry.getKey()).append(",")
                      .append(entry.getValue().get()).append("\n");
            });
        
        return report.toString();
    }
} 