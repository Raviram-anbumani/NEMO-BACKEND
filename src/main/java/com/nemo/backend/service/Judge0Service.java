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

    private static final String JUDGE0_URL =
            "https://ce.judge0.com";

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
            String submittedCode) {

        QuizQuestion question =
                quizDataService.getQuestion(questionId);

        if (question == null) {
            return CodeVerificationResult.failure(
                    "Question not found.");
        }

        if (!"code".equalsIgnoreCase(question.getType())) {
            return CodeVerificationResult.failure(
                    "Question is not a programming question.");
        }

        if (question.getFunctionName() == null ||
                question.getFunctionName().isBlank()) {

            return CodeVerificationResult.failure(
                    "Programming question is not configured correctly.");
        }

        List<JsonNode> hiddenTests = question.getHiddenTests();

        if (hiddenTests == null || hiddenTests.isEmpty()) {
            return CodeVerificationResult.failure(
                    "No hidden tests configured.");
        }

        if (submittedCode == null || submittedCode.isBlank()) {
            return CodeVerificationResult.failure(
                    "Code cannot be empty.");
        }

        List<String> failedTests = new ArrayList<>();

        for (int i = 0; i < hiddenTests.size(); i++) {

            JsonNode test = hiddenTests.get(i);

            JsonNode input = test.get("input");
            JsonNode expected = test.get("expected");

            if (input == null || expected == null) {
                return CodeVerificationResult.failure(
                        "Invalid hidden test configuration.");
            }

            try {

                String generatedSource =
                        buildTestSource(
                                submittedCode,
                                question.getFunctionName(),
                                input);

                JsonNode judgeResult =
                        executeOnJudge0(generatedSource);

                String actualOutput =
                        extractOutput(judgeResult);

                if (actualOutput == null) {
                    failedTests.add(
                            "Test " + (i + 1) + ": no valid output.");
                    continue;
                }

                JsonNode actual =
                        objectMapper.readTree(actualOutput);

                if (!actual.equals(expected)) {
                    failedTests.add(
                            "Test " + (i + 1) + " failed.");
                }

            } catch (Exception e) {

                failedTests.add(
                        "Test " + (i + 1) + " failed: "
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

    private String buildTestSource(
            String submittedCode,
            String functionName,
            JsonNode input) throws Exception {

        JsonNode inputObject = input;

        List<String> arguments = new ArrayList<>();

        inputObject.properties().forEach(entry -> {
            arguments.add(entry.getValue().toString());
        });

        String argumentList =
                String.join(", ", arguments);

        return submittedCode
                + "\n\n"
                + "const __nemo_result = "
                + functionName
                + "("
                + argumentList
                + ");\n"
                + "console.log(JSON.stringify(__nemo_result));\n";
    }

    private JsonNode executeOnJudge0(
            String sourceCode) throws Exception {

        String requestBody =
                objectMapper.writeValueAsString(
                        new Judge0Submission(
                                JAVASCRIPT_LANGUAGE_ID,
                                sourceCode,
                                MAX_TIME_LIMIT_SECONDS));

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                JUDGE0_URL
                                        + "/submissions?wait=true"))
                        .header(
                                "Content-Type",
                                "application/json")
                        .timeout(Duration.ofSeconds(15))
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        requestBody))
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "Judge0 returned HTTP "
                            + response.statusCode());
        }

        return objectMapper.readTree(
                response.body());
    }

    private String extractOutput(JsonNode judgeResult) {

        JsonNode status =
                judgeResult.get("status");

        if (status == null) {
            return null;
        }

        int statusId =
                status.get("id").asInt();

        // Judge0 status 3 = Accepted
        if (statusId != 3) {
            return null;
        }

        JsonNode stdout =
                judgeResult.get("stdout");

        if (stdout == null || stdout.isNull()) {
            return null;
        }

        return stdout.asText().trim();
    }

    public record Judge0Submission(
            int language_id,
            String source_code,
            int cpu_time_limit) {
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
}