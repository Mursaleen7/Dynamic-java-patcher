package com.example.patcher.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class StringConcatenationToStringBuilderTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new StringConcatenationToStringBuilder());
    }

    @Test
    void convertStringConcatenationInLoop() {
        rewriteRun(
            java(
                "package com.example.test;\n" +
                "\n" +
                "public class StringTest {\n" +
                "    public String concatenateInLoop(String[] items) {\n" +
                "        String result = \"\";\n" +
                "        for (int i = 0; i < items.length; i++) {\n" +
                "            result = result + items[i];\n" +
                "            if (i < items.length - 1) {\n" +
                "                result = result + \", \";\n" +
                "            }\n" +
                "        }\n" +
                "        return result;\n" +
                "    }\n" +
                "}",
                "package com.example.test;\n" +
                "\n" +
                "public class StringTest {\n" +
                "    public String concatenateInLoop(String[] items) {\n" +
                "        String result = \"\";\n" +
                "        StringBuilder sb_result_[0-9a-f]+ = new StringBuilder(result);\n" +
                "        for (int i = 0; i < items.length; i++) {\n" +
                "            sb_result_[0-9a-f]+.append(items[i]);\n" +
                "            if (i < items.length - 1) {\n" +
                "                sb_result_[0-9a-f]+.append(\", \");\n" +
                "            }\n" +
                "        }\n" +
                "        result = sb_result_[0-9a-f]+.toString();\n" +
                "        return result;\n" +
                "    }\n" +
                "}"
            )
        );
    }
} 