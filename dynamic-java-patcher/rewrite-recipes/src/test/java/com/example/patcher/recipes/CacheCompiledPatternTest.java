package com.example.patcher.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class CacheCompiledPatternTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CacheCompiledPattern());
    }

    @Test
    void cachePatternCompile() {
        rewriteRun(
            java(
                "package com.example.test;\n" +
                "\n" +
                "import java.util.regex.Matcher;\n" +
                "import java.util.regex.Pattern;\n" +
                "\n" +
                "public class PatternTest {\n" +
                "    public boolean matchesEmailFormat(String input) {\n" +
                "        Pattern emailPattern = Pattern.compile(\"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\");\n" +
                "        Matcher matcher = emailPattern.matcher(input);\n" +
                "        return matcher.matches();\n" +
                "    }\n" +
                "    \n" +
                "    public String extractDomain(String email) {\n" +
                "        Pattern emailPattern = Pattern.compile(\"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\");\n" +
                "        Matcher matcher = emailPattern.matcher(email);\n" +
                "        if (matcher.find()) {\n" +
                "            return matcher.group(1);\n" +
                "        }\n" +
                "        return null;\n" +
                "    }\n" +
                "}",
                "package com.example.test;\n" +
                "\n" +
                "import java.util.regex.Matcher;\n" +
                "import java.util.regex.Pattern;\n" +
                "\n" +
                "public class PatternTest {\n" +
                "    private static final java.util.regex.Pattern PATTERN_[a-zA-Z0-9_]+ = java.util.regex.Pattern.compile(\"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\\\\\.[a-zA-Z]{2,}\");\n" +
                "    \n" +
                "    public boolean matchesEmailFormat(String input) {\n" +
                "        Matcher matcher = PATTERN_[a-zA-Z0-9_]+.matcher(input);\n" +
                "        return matcher.matches();\n" +
                "    }\n" +
                "    \n" +
                "    public String extractDomain(String email) {\n" +
                "        Matcher matcher = PATTERN_[a-zA-Z0-9_]+.matcher(email);\n" +
                "        if (matcher.find()) {\n" +
                "            return matcher.group(1);\n" +
                "        }\n" +
                "        return null;\n" +
                "    }\n" +
                "}"
            )
        );
    }
} 