package com.testforge.api.model;

import java.util.List;

public class EndpointInfo {
    private String path;
    private String method;
    private String summary;
    private String description;
    private List<String> parameters;
    private String requestBodySchema;
    private List<Integer> responseCodes;
    private int riskScore; // computed: 1-10

    public EndpointInfo() {}

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getParameters() { return parameters; }
    public void setParameters(List<String> parameters) { this.parameters = parameters; }

    public String getRequestBodySchema() { return requestBodySchema; }
    public void setRequestBodySchema(String requestBodySchema) { this.requestBodySchema = requestBodySchema; }

    public List<Integer> getResponseCodes() { return responseCodes; }
    public void setResponseCodes(List<Integer> responseCodes) { this.responseCodes = responseCodes; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
}
