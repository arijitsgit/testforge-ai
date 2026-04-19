package com.testforge.api.model;

import java.util.Map;

public class TestCase {
    private String id;
    private String endpointPath;
    private String httpMethod;
    private String name;
    private String description;
    private String category; // HAPPY_PATH, BOUNDARY, AUTH_FAILURE, ERROR, VALIDATION
    private int riskScore;   // 1-10
    private String requestBody;
    private Map<String, String> headers;
    private int expectedStatusCode;
    private String expectedResponsePattern;
    private String generatedCode;

    public TestCase() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEndpointPath() { return endpointPath; }
    public void setEndpointPath(String endpointPath) { this.endpointPath = endpointPath; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public int getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(int expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }

    public String getExpectedResponsePattern() { return expectedResponsePattern; }
    public void setExpectedResponsePattern(String expectedResponsePattern) { this.expectedResponsePattern = expectedResponsePattern; }

    public String getGeneratedCode() { return generatedCode; }
    public void setGeneratedCode(String generatedCode) { this.generatedCode = generatedCode; }
}
