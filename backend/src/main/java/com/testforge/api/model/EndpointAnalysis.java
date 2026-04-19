package com.testforge.api.model;

import java.util.List;

public class EndpointAnalysis {
    private EndpointInfo endpoint;
    private List<TestCase> testCases;
    private String coverageStatus; // NONE, PARTIAL, COVERED
    private int riskScore;

    public EndpointAnalysis() {}

    public EndpointInfo getEndpoint() { return endpoint; }
    public void setEndpoint(EndpointInfo endpoint) { this.endpoint = endpoint; }

    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }

    public String getCoverageStatus() { return coverageStatus; }
    public void setCoverageStatus(String coverageStatus) { this.coverageStatus = coverageStatus; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
}
