/**
 * Runtime features for the SpeedDoctor framework using ByteBuddy.
 * <p>
 * This package contains three powerful ByteBuddy-based features:
 * <ul>
 *   <li><strong>Real-Time Lightweight Profiler</strong>: Adds timing counters to methods to identify 
 *       performance hotspots with minimal overhead.</li>
 *   <li><strong>Instant Deprecation Rescue</strong>: Provides runtime fallbacks for deprecated APIs, 
 *       allowing safe upgrades of dependencies without breaking changes.</li>
 *   <li><strong>Zero-Downtime Security Patches</strong>: Adds runtime sanitization to vulnerable methods 
 *       to prevent security issues until permanent fixes can be applied.</li>
 * </ul>
 * <p>
 * These features can be enabled/disabled using system properties:
 * <ul>
 *   <li><code>-Dspeeddoctor.profiler=true|false</code> (default: true)</li>
 *   <li><code>-Dspeeddoctor.deprecationrescue=true|false</code> (default: true)</li>
 *   <li><code>-Dspeeddoctor.securitypatches=true|false</code> (default: true)</li>
 * </ul>
 * <p>
 * Additional configuration options:
 * <ul>
 *   <li><code>-Dspeeddoctor.profiler.packages=com.example,org.springframework</code> - Comma-separated list of packages to profile (default: com.example,org.springframework,com.company)</li>
 *   <li><code>-Dspeeddoctor.deprecation.config=path/to/deprecation-mappings.json</code> - Path to JSON file with deprecation mappings (default: config/deprecation-mappings.json)</li>
 *   <li><code>-Dspeeddoctor.security.patterns=path/to/security-patterns.json</code> - Path to JSON file with security patterns (default: config/security-patterns.json)</li>
 * </ul>
 * <p>
 * To use these features, add the SpeedDoctor agent to your application startup:
 * <pre>
 * java -javaagent:patcher-agent.jar -jar myapp.jar
 * </pre>
 * 
 * <h3>Configuration File Formats</h3>
 * 
 * <h4>Deprecation Mappings (JSON)</h4>
 * <pre>
 * {
 *   "legacy.MathUtil": {
 *     "sum": "java.lang.Math#addExact",
 *     "multiply": "java.lang.Math#multiplyExact"
 *   },
 *   "legacy.FileUtils": {
 *     "deleteFile": "java.nio.file.Files#deleteIfExists"
 *   }
 * }
 * </pre>
 * 
 * <h4>Security Patterns (JSON)</h4>
 * <pre>
 * {
 *   "SQL_INJECTION": "(?i)('\\s*or\\s*'\\s*=\\s*')|('\\s*or\\s*1\\s*=\\s*1)|...",
 *   "XSS": "<script>|<\\/script>|javascript:|...",
 *   "PATH_TRAVERSAL": "\\.\\.(\\/|\\\\)|...",
 *   "COMMAND_INJECTION": ";\\s*(ls|dir|cat|...)"
 * }
 * </pre>
 * 
 * @see com.example.patcher.agent.features.ProfilerAdvice
 * @see com.example.patcher.agent.features.DeprecationRescueAdvice
 * @see com.example.patcher.agent.features.SecurityPatchAdvice
 * @see com.example.patcher.agent.features.FeatureTransformer
 */
package com.example.patcher.agent.features; 