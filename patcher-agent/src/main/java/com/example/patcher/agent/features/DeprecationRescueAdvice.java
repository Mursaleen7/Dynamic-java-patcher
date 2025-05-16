package com.example.patcher.agent.features;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import java.util.logging.Logger;

/**
 * Provides runtime fallback for deprecated API methods.
 * This class offers shims that delegate to newer API versions of common methods.
 */
public class DeprecationRescueAdvice {
    private static final Logger LOGGER = Logger.getLogger(DeprecationRescueAdvice.class.getName());

    /**
     * Example replacement for a deprecated Math utility method.
     * Redirects the deprecated method call to the new API method.
     * 
     * @param a First operand
     * @param b Second operand
     * @return The result of the new API call
     */
    @RuntimeType
    public static int legacySumShim(int a, int b) {
        LOGGER.fine("[DeprecationRescue] Redirecting legacy.MathUtil.sum to Math.addExact");
        return Math.addExact(a, b);
    }
    
    /**
     * Example replacement for a deprecated filesystem operation.
     * 
     * @param path The file path
     * @return True if successful
     */
    @RuntimeType
    public static boolean legacyFileDeleteShim(String path) {
        LOGGER.fine("[DeprecationRescue] Redirecting legacy file delete to Files.deleteIfExists");
        try {
            return java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path));
        } catch (Exception e) {
            LOGGER.warning("[DeprecationRescue] Error in file delete shim: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Example replacement for a deprecated string encoding method.
     * 
     * @param str The string to encode
     * @return The encoded string
     */
    @RuntimeType
    public static String legacyUrlEncodeShim(String str) {
        LOGGER.fine("[DeprecationRescue] Redirecting legacy URL encoding to StandardCharsets");
        try {
            return java.net.URLEncoder.encode(str, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            LOGGER.warning("[DeprecationRescue] Error in URL encoding shim: " + e.getMessage());
            return str;
        }
    }
} 