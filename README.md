# Dynamic Java Patcher

A toolkit for patching Java applications at runtime, with both runtime patching (ByteBuddy) and build-time fix integration (OpenRewrite).

## Features

- **Dynamic Patching**: Apply bytecode patches to running applications without restart
- **HTTP/File System Patch Delivery**: Fetch patches from a remote server or local file system
- **Patch Versioning**: Track and apply patches in order with versioning
- **SpeedDoctor**: Runtime performance, security and API compatibility features

## SpeedDoctor Features

The SpeedDoctor components provide three powerful features:

### 1. Real-Time Lightweight Profiler

Identifies performance hotspots in your application with minimal overhead (<1%).

- Collects timing data for method execution
- Logs slow methods and generates reports
- Produces CSV output that can be used by OpenRewrite recipes to target optimizations

### 2. Instant Deprecation Rescue

Provides runtime fallbacks for deprecated APIs, allowing safe dependency upgrades without breaking changes.

- Intercepts calls to deprecated methods and redirects to newer alternatives
- Works alongside OpenRewrite recipes that permanently update the code
- Configurable through JSON files to add new deprecation mappings

### 3. Zero-Downtime Security Patches

Adds runtime sanitization to vulnerable methods to prevent security issues until permanent fixes can be applied.

- Protects against SQL injection, XSS, path traversal, and more
- Works alongside OpenRewrite recipes that permanently secure the code
- Configurable through JSON files to add new security patterns

## Configuration

### Basic Configuration

The SpeedDoctor features can be enabled/disabled using system properties:

```
-Dspeeddoctor.profiler=true|false (default: true)
-Dspeeddoctor.deprecationrescue=true|false (default: true)
-Dspeeddoctor.securitypatches=true|false (default: true)
```

### Advanced Configuration

Additional configuration options:

```
# Profiler configuration
-Dspeeddoctor.profiler.packages=com.example,org.springframework
  Comma-separated list of packages to profile
  (default: com.example,org.springframework,com.company)

# Deprecation rescue configuration
-Dspeeddoctor.deprecation.config=path/to/deprecation-mappings.json
  Path to JSON file with deprecation mappings
  (default: config/deprecation-mappings.json)

# Security patches configuration  
-Dspeeddoctor.security.patterns=path/to/security-patterns.json
  Path to JSON file with security patterns
  (default: config/security-patterns.json)
```

### Configuration File Formats

#### Deprecation Mappings (JSON)

```json
{
  "legacy.MathUtil": {
    "sum": "java.lang.Math#addExact",
    "multiply": "java.lang.Math#multiplyExact"
  },
  "legacy.FileUtils": {
    "deleteFile": "java.nio.file.Files#deleteIfExists"
    }
}
```

#### Security Patterns (JSON)

```json
{
  "SQL_INJECTION": "(?i)('\\s*or\\s*'\\s*=\\s*')|('\\s*or\\s*1\\s*=\\s*1)|...",
  "XSS": "<script>|<\\/script>|javascript:|...",
  "PATH_TRAVERSAL": "\\.\\.(\\/|\\\\)|...",
  "COMMAND_INJECTION": ";\\s*(ls|dir|cat|...)"
}
```

## Build and Run

Build the project:

```shell
mvn clean package
```

Run with the agent:

```shell
java -javaagent:patcher-agent/target/patcher-agent-1.0-SNAPSHOT.jar -jar your-application.jar
```

## Using OpenRewrite Recipes

The project includes several OpenRewrite recipes for permanent fixes:

- `HotspotRecipe`: Optimizes classes identified as hotspots by the profiler
- `ApiModernizationRecipe`: Updates deprecated API calls to their modern equivalents
- `SecurityPatchRecipe`: Adds sanitization to methods vulnerable to security attacks

Add to your Maven build:

```xml
<plugin>
  <groupId>org.openrewrite.maven</groupId>
  <artifactId>rewrite-maven-plugin</artifactId>
  <version>4.46.0</version>
  <configuration>
    <activeRecipes>
      <recipe>com.example.patcher.recipes.speeddoctor.HotspotRecipe</recipe>
      <recipe>com.example.patcher.recipes.speeddoctor.ApiModernizationRecipe</recipe>
      <recipe>com.example.patcher.recipes.speeddoctor.SecurityPatchRecipe</recipe>
    </activeRecipes>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>com.example.patcher</groupId>
      <artifactId>rewrite-recipes</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
</plugin>
```

## License

See the LICENSE file for details. 