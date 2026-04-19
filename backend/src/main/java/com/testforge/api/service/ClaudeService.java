package com.testforge.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testforge.api.model.EndpointInfo;
import com.testforge.api.model.TestCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ClaudeService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";

    @Value("${anthropic.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ClaudeService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public Map<String, List<TestCase>> generateTestCasesForAll(List<EndpointInfo> endpoints) {
        String response = callClaude(buildBatchTestCasePrompt(endpoints));
        return parseBatchTestCases(response, endpoints);
    }

    public Map<String, List<TestCase>> analyzeCoverageGapsForAll(List<EndpointInfo> endpoints, String existingTestCode) {
        String response = callClaude(buildBatchCoverageGapPrompt(endpoints, existingTestCode));
        return parseBatchTestCases(response, endpoints);
    }

    public String generateJUnitCode(TestCase testCase, String baseUrl, String patternCode) {
        return callClaude(buildJUnitCodePrompt(testCase, baseUrl, patternCode));
    }

    @SuppressWarnings("unchecked")
    private String callClaude(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> message = Map.of("role", "user", "content", userMessage);
        Map<String, Object> body = Map.of(
            "model", MODEL,
            "max_tokens", 8192,
            "messages", List.of(message)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
            if (content != null && !content.isEmpty()) {
                return (String) content.get(0).get("text");
            }
        }
        return "{}";
    }

    private String buildBatchTestCasePrompt(List<EndpointInfo> endpoints) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            You are a senior QA engineer specializing in fintech API testing.
            Generate thorough test cases for ALL endpoints below in ONE response.

            For EACH endpoint apply these techniques:
            1. BOUNDARY VALUE ANALYSIS (BVA):
               - For numeric fields: min, max, min-1, max+1, zero, negative, very large number
               - For strings: empty string, null, max length, max+1 length, special characters
               - For enums: each valid value, invalid value, null
               - For dates: past, future, today, invalid format
            2. EQUIVALENCE PARTITIONING: valid partition, invalid partition per field
            3. FINTECH-SPECIFIC EDGE CASES:
               - Amount: 0.00, 0.01, negative, 999999999.99, more than 2 decimal places
               - Currency: each valid currency, unsupported currency, mixed case
               - IDs: non-existent ID, malformed UUID, SQL injection string
               - Auth: missing token, expired token, wrong permissions
            4. ERROR SCENARIOS: 400, 401, 403, 404, 409, 422, 500
            5. HAPPY PATH: successful request with valid data

            Generate 6-10 test cases per endpoint.

            Return ONLY a JSON object (no markdown, no extra text):
            {
              "POST /api/subscriptions": [
                {
                  "name": "short descriptive name",
                  "description": "exactly what is being tested and why it matters",
                  "category": "BOUNDARY",
                  "riskScore": 9,
                  "requestBody": "{\"amount\": -1, \"currency\": \"USD\"}",
                  "expectedStatusCode": 400,
                  "expectedResponsePattern": "error response with message about invalid amount"
                }
              ]
            }
            Valid categories: HAPPY_PATH, BOUNDARY, AUTH_FAILURE, ERROR, VALIDATION

            Endpoints:
            """);

        for (EndpointInfo e : endpoints) {
            sb.append("\n- %s %s | summary: %s | params: %s | requestBody: %s | responses: %s | risk: %d/10"
                .formatted(e.getMethod(), e.getPath(), e.getSummary(),
                    e.getParameters(), e.getRequestBodySchema(),
                    e.getResponseCodes(), e.getRiskScore()));
        }
        return sb.toString();
    }

    private String buildBatchCoverageGapPrompt(List<EndpointInfo> endpoints, String testCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            You are a senior QA engineer. Analyze the existing test code and identify MISSING test cases
            for each endpoint, focusing on:
            1. BVA gaps: boundary values not tested (min-1, max+1, zero, null, empty)
            2. Fintech gaps: negative amounts, currency edge cases, large numbers
            3. Auth gaps: missing 401/403 scenarios
            4. Error gaps: untested error codes
            5. Validation gaps: missing field-level validation tests

            Return ONLY a JSON object (no markdown):
            {
              "POST /api/subscriptions": [
                {
                  "name": "gap test name",
                  "description": "what gap this fills and why it matters",
                  "category": "BOUNDARY",
                  "riskScore": 8,
                  "requestBody": "{\"amount\": 0}",
                  "expectedStatusCode": 400,
                  "expectedResponsePattern": "validation error for zero amount"
                }
              ]
            }
            If fully covered, use []. Valid categories: HAPPY_PATH, BOUNDARY, AUTH_FAILURE, ERROR, VALIDATION

            Endpoints:
            """);

        for (EndpointInfo e : endpoints) {
            sb.append("\n- %s %s | summary: %s | params: %s | body: %s | responses: %s"
                .formatted(e.getMethod(), e.getPath(), e.getSummary(),
                    e.getParameters(), e.getRequestBodySchema(), e.getResponseCodes()));
        }

        sb.append("\n\nExisting Test Code:\n").append(testCode);
        return sb.toString();
    }

    private String buildJUnitCodePrompt(TestCase testCase, String baseUrl, String patternCode) {
        String patternSection = (patternCode != null && !patternCode.isBlank())
            ? """

              IMPORTANT — Mirror this existing test pattern EXACTLY:
              - Use the same base class, imports, and annotations
              - Follow the same naming convention for test methods
              - Use the same assertion library and style
              - Reuse the same helper methods if present
              - Match indentation and formatting style

              Existing pattern to follow:
              %s
              """.formatted(patternCode)
            : """

              Use standard JUnit 5 + RestAssured pattern:
              - @Test, @DisplayName annotations
              - given().header().body() / .when().post() / .then().statusCode().body() structure
              - Hamcrest matchers for assertions
              """;

        return """
            You are a senior Java test engineer. Generate a complete, runnable test method for this test case.

            Base URL: %s
            Endpoint: %s %s
            Test Name: %s
            Description: %s
            Category: %s
            Request Body: %s
            Expected Status: %d
            Expected Response: %s
            %s
            Requirements:
            - Apply BVA: use exact boundary values in request body where applicable
            - Include inline comment explaining WHY this boundary/edge case matters
            - Include all necessary imports
            - Make it copy-paste ready with no placeholders

            Return ONLY Java code. No markdown fences. No explanation outside the code.
            """.formatted(
                baseUrl != null ? baseUrl : "http://localhost:8080",
                testCase.getHttpMethod(), testCase.getEndpointPath(),
                testCase.getName(), testCase.getDescription(), testCase.getCategory(),
                testCase.getRequestBody(), testCase.getExpectedStatusCode(),
                testCase.getExpectedResponsePattern(), patternSection
        );
    }

    private Map<String, List<TestCase>> parseBatchTestCases(String response, List<EndpointInfo> endpoints) {
        Map<String, List<TestCase>> result = new LinkedHashMap<>();
        for (EndpointInfo e : endpoints) {
            result.put(e.getMethod() + " " + e.getPath(), new ArrayList<>());
        }

        try {
            String json = extractJson(response);
            JsonNode root = objectMapper.readTree(json);
            root.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                List<TestCase> cases = new ArrayList<>();
                entry.getValue().forEach(node -> {
                    try {
                        TestCase tc = objectMapper.treeToValue(node, TestCase.class);
                        tc.setId(UUID.randomUUID().toString());
                        String[] parts = key.split(" ", 2);
                        if (parts.length == 2) {
                            tc.setHttpMethod(parts[0]);
                            tc.setEndpointPath(parts[1]);
                        }
                        cases.add(tc);
                    } catch (Exception ignored) {}
                });
                result.put(key, cases);
            });
        } catch (Exception ignored) {}

        return result;
    }

    private String extractJson(String response) {
        if (response == null) return "{}";
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) return response.substring(start, end + 1);
        return "{}";
    }
}
