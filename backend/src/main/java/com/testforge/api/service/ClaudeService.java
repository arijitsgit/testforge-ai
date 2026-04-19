package com.testforge.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
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

    // Single batched call for all endpoints
    public Map<String, List<TestCase>> generateTestCasesForAll(List<EndpointInfo> endpoints) {
        String prompt = buildBatchTestCasePrompt(endpoints);
        String response = callClaude(prompt);
        return parseBatchTestCases(response, endpoints);
    }

    // Single batched call for coverage gaps
    public Map<String, List<TestCase>> analyzeCoverageGapsForAll(List<EndpointInfo> endpoints, String existingTestCode) {
        String prompt = buildBatchCoverageGapPrompt(endpoints, existingTestCode);
        String response = callClaude(prompt);
        return parseBatchTestCases(response, endpoints);
    }

    public String generateJUnitCode(TestCase testCase, String baseUrl) {
        return callClaude(buildJUnitCodePrompt(testCase, baseUrl));
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
            You are an expert QA engineer specializing in fintech API testing.
            Generate comprehensive test cases for ALL of the following API endpoints in ONE response.

            For each endpoint, generate 3-5 test cases covering: happy path, boundary values,
            auth failures, error handling, validation, and fintech edge cases
            (negative amounts, zero values, currency mismatches, large numbers).

            Return ONLY a JSON object (no markdown, no extra text) where each key is "METHOD /path"
            and the value is an array of test cases:
            {
              "POST /api/subscriptions": [
                {
                  "name": "test case name",
                  "description": "what this tests",
                  "category": "HAPPY_PATH",
                  "riskScore": 7,
                  "requestBody": null,
                  "expectedStatusCode": 201,
                  "expectedResponsePattern": "description of expected response"
                }
              ]
            }
            Valid categories: HAPPY_PATH, BOUNDARY, AUTH_FAILURE, ERROR, VALIDATION

            Endpoints to analyze:
            """);

        for (EndpointInfo e : endpoints) {
            sb.append("\n- %s %s: %s | params: %s | body: %s | responses: %s | risk: %d/10"
                .formatted(e.getMethod(), e.getPath(), e.getSummary(),
                    e.getParameters(), e.getRequestBodySchema(), e.getResponseCodes(), e.getRiskScore()));
        }
        return sb.toString();
    }

    private String buildBatchCoverageGapPrompt(List<EndpointInfo> endpoints, String testCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            You are an expert QA engineer. Analyze the existing test code below and identify
            MISSING test cases for each API endpoint listed.

            Return ONLY a JSON object (no markdown, no extra text) where each key is "METHOD /path"
            and the value is an array of MISSING test cases:
            {
              "POST /api/subscriptions": [
                {
                  "name": "missing test name",
                  "description": "what gap this fills",
                  "category": "BOUNDARY",
                  "riskScore": 8,
                  "requestBody": null,
                  "expectedStatusCode": 400,
                  "expectedResponsePattern": "description of expected response"
                }
              ]
            }
            If an endpoint is fully covered, use an empty array [].
            Valid categories: HAPPY_PATH, BOUNDARY, AUTH_FAILURE, ERROR, VALIDATION

            Endpoints:
            """);

        for (EndpointInfo e : endpoints) {
            sb.append("\n- %s %s: %s | params: %s | body: %s | responses: %s"
                .formatted(e.getMethod(), e.getPath(), e.getSummary(),
                    e.getParameters(), e.getRequestBodySchema(), e.getResponseCodes()));
        }

        sb.append("\n\nExisting Test Code:\n").append(testCode);
        return sb.toString();
    }

    private String buildJUnitCodePrompt(TestCase testCase, String baseUrl) {
        return """
            Generate a complete JUnit 5 test method using RestAssured.

            Base URL: %s
            Endpoint: %s %s
            Test Name: %s
            Description: %s
            Category: %s
            Request Body: %s
            Expected Status: %d
            Expected Response: %s

            Use @Test, @DisplayName, given/when/then structure, proper assertions.
            Include imports. Return ONLY Java code, no markdown.
            """.formatted(
                baseUrl != null ? baseUrl : "http://localhost:8080",
                testCase.getHttpMethod(), testCase.getEndpointPath(),
                testCase.getName(), testCase.getDescription(), testCase.getCategory(),
                testCase.getRequestBody(), testCase.getExpectedStatusCode(),
                testCase.getExpectedResponsePattern()
        );
    }

    private Map<String, List<TestCase>> parseBatchTestCases(String response, List<EndpointInfo> endpoints) {
        Map<String, List<TestCase>> result = new LinkedHashMap<>();

        // Pre-populate with empty lists so all endpoints appear even if Claude skips one
        for (EndpointInfo e : endpoints) {
            result.put(e.getMethod() + " " + e.getPath(), new ArrayList<>());
        }

        try {
            String json = extractJson(response, '{', '}');
            JsonNode root = objectMapper.readTree(json);

            root.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                List<TestCase> cases = new ArrayList<>();
                entry.getValue().forEach(node -> {
                    try {
                        TestCase tc = objectMapper.treeToValue(node, TestCase.class);
                        tc.setId(UUID.randomUUID().toString());
                        // Match key "METHOD /path" back to endpoint
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

    private String extractJson(String response, char open, char close) {
        if (response == null) return open == '[' ? "[]" : "{}";
        int start = response.indexOf(open);
        int end = response.lastIndexOf(close);
        if (start >= 0 && end > start) return response.substring(start, end + 1);
        return open == '[' ? "[]" : "{}";
    }
}
