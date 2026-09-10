package com.nemo.backend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nemo.backend.model.quiz.QuizQuestion;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class Judge0Service {

    private static final String JUDGE0_URL = "https://ce.judge0.com";

    /*
         * Judge0 language IDs.
         *
         * Java -> 91
         * Python -> 100
         * JavaScript -> 102
         *
         * If your Judge0 instance reports different IDs,
         * update these values.
     */
    private static final int JAVA_LANGUAGE_ID = 91;
    private static final int PYTHON_LANGUAGE_ID = 100;
    private static final int JAVASCRIPT_LANGUAGE_ID = 102;

    private static final int MAX_TIME_LIMIT_SECONDS = 5;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final QuizDataService quizDataService;

    public Judge0Service(
            QuizDataService quizDataService,
            ObjectMapper objectMapper) {

        this.quizDataService = quizDataService;
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CodeVerificationResult verifyCode(
            String questionId,
            String submittedCode,
            String language) {

        QuizQuestion question = quizDataService.getQuestion(questionId);

        if (question == null) {
            return CodeVerificationResult.failure(
                    "Question not found.");
        }

        if (!"code".equalsIgnoreCase(question.getType())) {
            return CodeVerificationResult.failure(
                    "Question is not a programming question.");
        }

        if (question.getFunctionName() == null
                || question.getFunctionName().isBlank()) {

            return CodeVerificationResult.failure(
                    "Programming question is not configured correctly.");
        }

        if (submittedCode == null
                || submittedCode.isBlank()) {

            return CodeVerificationResult.failure(
                    "Code cannot be empty.");
        }

        if (language == null
                || language.isBlank()) {

            return CodeVerificationResult.failure(
                    "Please select a programming language.");
        }

        int languageId;

        try {
            languageId = getLanguageId(language);
        } catch (IllegalArgumentException e) {
            return CodeVerificationResult.failure(
                    e.getMessage());
        }

        List<JsonNode> hiddenTests = question.getHiddenTests();

        if (hiddenTests == null
                || hiddenTests.isEmpty()) {

            return CodeVerificationResult.failure(
                    "No hidden tests configured.");
        }

        List<String> failedTests = new ArrayList<>();

        for (int i = 0; i < hiddenTests.size(); i++) {

            JsonNode test = hiddenTests.get(i);

            JsonNode input = test.get("input");

            JsonNode expected = test.get("expected");

            if (input == null
                    || expected == null) {

                return CodeVerificationResult.failure(
                        "Invalid hidden test configuration.");
            }

            try {

                String generatedSource = buildTestSource(
                        submittedCode,
                        question.getFunctionName(),
                        input,
                        language);

                JsonNode judgeResult = executeOnJudge0(
                        generatedSource,
                        languageId);

                String actualOutput = extractOutput(judgeResult);

                if (actualOutput == null) {

                    String error = extractJudgeError(judgeResult);

                    failedTests.add(
                            "Test " + (i + 1)
                            + " failed."
                            + (error.isBlank()
                            ? ""
                            : " " + error));

                    continue;
                }

                JsonNode actual = objectMapper.readTree(
                        actualOutput);

                if (!actual.equals(expected)) {

                    failedTests.add(
                            "Test " + (i + 1)
                            + " failed. Expected "
                            + expected
                            + " but got "
                            + actual
                            + ".");
                }

            } catch (Exception e) {

                failedTests.add(
                        "Test " + (i + 1)
                        + " failed: "
                        + e.getMessage());
            }
        }

        if (!failedTests.isEmpty()) {

            return CodeVerificationResult.failure(
                    "Some hidden tests failed.",
                    failedTests);
        }

        return CodeVerificationResult.success(
                "All hidden tests passed.");
    }

    // ---------------------------------------------------------
// PUBLIC / VISIBLE TEST CASES
// ---------------------------------------------------------
    public List<VisibleTestResult> runVisibleTests(
            String questionId,
            String submittedCode,
            String language) {

        QuizQuestion question = quizDataService.getQuestion(questionId);

        if (question == null) {
            throw new IllegalArgumentException(
                    "Question not found.");
        }

        if (!"code".equalsIgnoreCase(question.getType())) {
            throw new IllegalArgumentException(
                    "Question is not a programming question.");
        }

        if (question.getFunctionName() == null
                || question.getFunctionName().isBlank()) {

            throw new IllegalArgumentException(
                    "Programming question is not configured correctly.");
        }

        if (submittedCode == null
                || submittedCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Code cannot be empty.");
        }

        if (language == null
                || language.isBlank()) {

            throw new IllegalArgumentException(
                    "Please select a programming language.");
        }

        int languageId = getLanguageId(language);

        List<JsonNode> sampleTests
                = question.getSampleTests();

        if (sampleTests == null
                || sampleTests.isEmpty()) {

            throw new IllegalArgumentException(
                    "No sample test cases configured for this question.");
        }

        List<VisibleTestResult> results
                = new ArrayList<>();

        for (int i = 0; i < sampleTests.size(); i++) {

            JsonNode test = sampleTests.get(i);

            JsonNode input = test.get("input");
            JsonNode expected = test.get("expected");

            if (input == null
                    || expected == null) {

                results.add(
                        new VisibleTestResult(
                                i + 1,
                                input,
                                expected,
                                null,
                                false,
                                "Invalid sample test configuration."
                        )
                );

                continue;
            }

            try {

                String generatedSource
                        = buildTestSource(
                                submittedCode,
                                question.getFunctionName(),
                                input,
                                language
                        );

                JsonNode judgeResult
                        = executeOnJudge0(
                                generatedSource,
                                languageId
                        );

                String actualOutput
                        = extractOutput(judgeResult);

                if (actualOutput == null) {

                    String error
                            = extractJudgeError(judgeResult);

                    results.add(
                            new VisibleTestResult(
                                    i + 1,
                                    input,
                                    expected,
                                    null,
                                    false,
                                    error.isBlank()
                                    ? "Code execution failed."
                                    : error
                            )
                    );

                    continue;
                }

                JsonNode actual
                        = objectMapper.readTree(actualOutput);

                boolean passed
                        = actual.equals(expected);

                results.add(
                        new VisibleTestResult(
                                i + 1,
                                input,
                                expected,
                                actual,
                                passed,
                                passed
                                        ? "Passed"
                                        : "Expected "
                                        + expected
                                        + " but got "
                                        + actual
                        )
                );

            } catch (Exception e) {

                results.add(
                        new VisibleTestResult(
                                i + 1,
                                input,
                                expected,
                                null,
                                false,
                                e.getMessage() == null
                                ? "Execution failed."
                                : e.getMessage()
                        )
                );
            }
        }

        return results;
    }
    // ---------------------------------------------------------
    // LANGUAGE SELECTION
    // ---------------------------------------------------------

    private int getLanguageId(
            String language) {

        String normalized = language.trim().toLowerCase();

        return switch (normalized) {

            case "java" ->
                JAVA_LANGUAGE_ID;

            case "python", "python3" ->
                PYTHON_LANGUAGE_ID;

            case "javascript", "js" ->
                JAVASCRIPT_LANGUAGE_ID;

            default ->
                throw new IllegalArgumentException(
                        "Unsupported language: "
                        + language
                        + ". Supported languages are "
                        + "Java, Python and JavaScript.");
        };
    }

    // ---------------------------------------------------------
    // SOURCE GENERATION
    // ---------------------------------------------------------
    private String buildTestSource(
            String submittedCode,
            String functionName,
            JsonNode input,
            String language)
            throws Exception {

        String normalized = language.trim().toLowerCase();

        return switch (normalized) {

            case "java" ->
                buildJavaTestSource(
                submittedCode,
                functionName,
                input);

            case "python", "python3" ->
                buildPythonTestSource(
                submittedCode,
                functionName,
                input);

            case "javascript", "js" ->
                buildJavaScriptTestSource(
                submittedCode,
                functionName,
                input);

            default ->
                throw new IllegalArgumentException(
                        "Unsupported language: "
                        + language);
        };
    }

    // ---------------------------------------------------------
    // JAVASCRIPT
    // ---------------------------------------------------------
    private String buildJavaScriptTestSource(
            String submittedCode,
            String functionName,
            JsonNode input)
            throws Exception {

        List<String> arguments = getJsonArguments(input);

        String argumentList = String.join(", ", arguments);

        boolean usesSolutionClass = submittedCode.contains(
                "class Solution");

        String invocation;

        if (usesSolutionClass) {

            invocation = "const __nemo_solution = new Solution();\n"
                    + "const __nemo_result = "
                    + "__nemo_solution."
                    + functionName
                    + "("
                    + argumentList
                    + ");\n";

        } else {

            invocation = "const __nemo_result = "
                    + functionName
                    + "("
                    + argumentList
                    + ");\n";
        }

        return submittedCode
                + "\n\n"
                + invocation
                + "console.log("
                + "JSON.stringify(__nemo_result)"
                + ");\n";
    }

    // ---------------------------------------------------------
    // PYTHON
    // ---------------------------------------------------------
    private String buildPythonTestSource(
            String submittedCode,
            String functionName,
            JsonNode input)
            throws Exception {

        List<String> arguments = getJsonArguments(input);

        String argumentList = String.join(", ", arguments);

        StringBuilder source = new StringBuilder();

        source.append(
                "import json\n\n");

        source.append(
                submittedCode);

        source.append(
                "\n\n");

        /*
                 * Supports both:
                 *
                 * def canPlaceFlowers(...):
                 *
                 * and:
                 *
                 * class Solution:
                 * def canPlaceFlowers(...):
         */
        source.append(
                "if 'Solution' in globals() "
                + "and hasattr(Solution, '"
                + functionName
                + "'):\n");

        source.append(
                "    __nemo_solution = Solution()\n");

        source.append(
                "    __nemo_result = "
                + "__nemo_solution."
                + functionName
                + "("
                + argumentList
                + ")\n");

        source.append(
                "else:\n");

        source.append(
                "    __nemo_result = "
                + functionName
                + "("
                + argumentList
                + ")\n");

        source.append(
                "\n");

        source.append(
                "print(json.dumps(__nemo_result))\n");

        return source.toString();
    }

    // ---------------------------------------------------------
    // JAVA
    // ---------------------------------------------------------
    private String buildJavaTestSource(
            String submittedCode,
            String functionName,
            JsonNode input)
            throws Exception {

        List<String> arguments = new ArrayList<>();

        input.properties().forEach(
                entry -> {

                    String javaValue = jsonToJavaLiteral(
                            entry.getValue());

                    arguments.add(
                            javaValue);
                });

        String argumentList = String.join(
                ", ",
                arguments);

        /*
                 * The submitted code is expected to contain:
                 *
                 * class Solution {
                 * public boolean canPlaceFlowers(...) {
                 * ...
                 * }
                 * }
         */
        return submittedCode
                + "\n\n"
                + "public class Main {\n"
                + "    public static void main(String[] args) {\n"
                + "        Solution solution = new Solution();\n"
                + "\n"
                + "        Object __nemo_result = "
                + "solution."
                + functionName
                + "("
                + argumentList
                + ");\n"
                + "\n"
                + "        System.out.println("
                + "NemoJson.toJson(__nemo_result)"
                + ");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class NemoJson {\n"
                + "    static String toJson(Object value) {\n"
                + "        if (value == null) {\n"
                + "            return \"null\";\n"
                + "        }\n"
                + "\n"
                + "        if (value instanceof String) {\n"
                + "            return \"\\\\\\\"\"\n"
                + "                    + ((String) value)\n"
                + "                    .replace(\"\\\\\", \"\\\\\\\\\")\n"
                + "                    .replace(\"\\\"\", \"\\\\\\\"\")\n"
                + "                    + \"\\\\\\\"\";\n"
                + "        }\n"
                + "\n"
                + "        if (value instanceof Character) {\n"
                + "            return \"\\\\\\\"\"\n"
                + "                    + value\n"
                + "                    + \"\\\\\\\"\";\n"
                + "        }\n"
                + "\n"
                + "        if (value instanceof Number ||\n"
                + "            value instanceof Boolean) {\n"
                + "            return String.valueOf(value);\n"
                + "        }\n"
                + "\n"
                + "        if (value.getClass().isArray()) {\n"
                + "            StringBuilder sb = new StringBuilder();\n"
                + "            sb.append(\"[\");\n"
                + "\n"
                + "            int length =\n"
                + "                    java.lang.reflect.Array\n"
                + "                    .getLength(value);\n"
                + "\n"
                + "            for (int i = 0; i < length; i++) {\n"
                + "                if (i > 0) {\n"
                + "                    sb.append(\",\");\n"
                + "                }\n"
                + "\n"
                + "                Object element =\n"
                + "                        java.lang.reflect.Array\n"
                + "                        .get(value, i);\n"
                + "\n"
                + "                sb.append(toJson(element));\n"
                + "            }\n"
                + "\n"
                + "            sb.append(\"]\");\n"
                + "            return sb.toString();\n"
                + "        }\n"
                + "\n"
                + "        return \"null\";\n"
                + "    }\n"
                + "}\n";
    }

    // ---------------------------------------------------------
    // JSON -> JAVA LITERALS
    // ---------------------------------------------------------
    private String jsonToJavaLiteral(
            JsonNode node) {

        if (node.isInt()
                || node.isLong()
                || node.isShort()) {

            return node.asText();
        }

        if (node.isDouble()
                || node.isFloat()
                || node.isBigDecimal()) {

            return node.asText();
        }

        if (node.isBoolean()) {

            return node.asText();
        }

        if (node.isTextual()) {

            return "\""
                    + escapeJavaString(
                            node.asText())
                    + "\"";
        }

        if (node.isArray()) {

            return buildJavaArrayLiteral(
                    node);
        }

        if (node.isNull()) {

            return "null";
        }

        throw new IllegalArgumentException(
                "Unsupported JSON input type: "
                + node);
    }

    private String buildJavaArrayLiteral(
            JsonNode array) {

        if (array.isEmpty()) {

            return "new int[]{}";
        }

        boolean nested = array.get(0).isArray();

        StringBuilder sb = new StringBuilder();

        if (nested) {

            sb.append("new int[][]{");

        } else {

            sb.append("new int[]{");
        }

        for (int i = 0; i < array.size(); i++) {

            if (i > 0) {
                sb.append(", ");
            }

            sb.append(
                    jsonToJavaLiteral(
                            array.get(i)));
        }

        sb.append("}");

        return sb.toString();
    }

    // ---------------------------------------------------------
    // JSON ARGUMENTS
    // ---------------------------------------------------------
    private List<String> getJsonArguments(
            JsonNode input) {

        List<String> arguments = new ArrayList<>();

        input.properties().forEach(
                entry -> {

                    arguments.add(
                            entry.getValue()
                                    .toString());
                });

        return arguments;
    }

    // ---------------------------------------------------------
    // JUDGE0
    // ---------------------------------------------------------
    private JsonNode executeOnJudge0(
            String sourceCode,
            int languageId)
            throws Exception {

        String requestBody = objectMapper.writeValueAsString(
                new Judge0Submission(
                        languageId,
                        sourceCode,
                        MAX_TIME_LIMIT_SECONDS));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        JUDGE0_URL
                        + "/submissions?wait=true"))
                .header(
                        "Content-Type",
                        "application/json")
                .timeout(
                        Duration.ofSeconds(15))
                .POST(
                        HttpRequest.BodyPublishers
                                .ofString(
                                        requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers
                        .ofString());

        if (response.statusCode() != 200
                && response.statusCode() != 201) {

            throw new RuntimeException(
                    "Judge0 returned HTTP "
                    + response.statusCode()
                    + ": "
                    + response.body());
        }

        return objectMapper.readTree(
                response.body());
    }

    // ---------------------------------------------------------
    // OUTPUT
    // ---------------------------------------------------------
    private String extractOutput(
            JsonNode judgeResult) {

        JsonNode status = judgeResult.get("status");

        if (status == null) {
            return null;
        }

        int statusId = status.get("id").asInt();

        // Judge0 status 3 = Accepted
        if (statusId != 3) {
            return null;
        }

        JsonNode stdout = judgeResult.get("stdout");

        if (stdout == null
                || stdout.isNull()) {

            return null;
        }

        return stdout
                .asText()
                .trim();
    }

    private String extractJudgeError(
            JsonNode judgeResult) {

        StringBuilder error = new StringBuilder();

        JsonNode status = judgeResult.get("status");

        if (status != null
                && status.get("description") != null) {

            error.append(
                    status.get("description")
                            .asText());
        }

        JsonNode stderr = judgeResult.get("stderr");

        if (stderr != null
                && !stderr.isNull()
                && !stderr.asText().isBlank()) {

            if (error.length() > 0) {
                error.append(": ");
            }

            error.append(
                    stderr.asText().trim());
        }

        JsonNode compileOutput = judgeResult.get("compile_output");

        if (compileOutput != null
                && !compileOutput.isNull()
                && !compileOutput.asText().isBlank()) {

            if (error.length() > 0) {
                error.append(": ");
            }

            error.append(
                    compileOutput.asText().trim());
        }

        return error.toString();
    }

    // ---------------------------------------------------------
    // RECORDS
    // ---------------------------------------------------------
    public record Judge0Submission(
            int language_id,
            String source_code,
            int cpu_time_limit) {

    }

    public record VisibleTestResult(
            int testNumber,
            JsonNode input,
            JsonNode expected,
            JsonNode actual,
            boolean passed,
            String message
            ) {

    }

    public record CodeVerificationResult(
            boolean success,
            String message,
            List<String> failedTests) {

        public static CodeVerificationResult success(
                String message) {

            return new CodeVerificationResult(
                    true,
                    message,
                    List.of());
        }

        public static CodeVerificationResult failure(
                String message) {

            return new CodeVerificationResult(
                    false,
                    message,
                    List.of());
        }

        public static CodeVerificationResult failure(
                String message,
                List<String> failedTests) {

            return new CodeVerificationResult(
                    false,
                    message,
                    failedTests);
        }
    }

    private String escapeJavaString(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

}
