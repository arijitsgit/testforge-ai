package com.testforge.api.model;

import java.util.List;

public class AnalysisResult {
    private String specTitle;
    private String specVersion;
    private int totalEndpoints;
    private int totalTestCases;
    private List<EndpointAnalysis> endpoints;

    public AnalysisResult() {}

    public String getSpecTitle() { return specTitle; }
    public void setSpecTitle(String specTitle) { this.specTitle = specTitle; }

    public String getSpecVersion() { return specVersion; }
    public void setSpecVersion(String specVersion) { this.specVersion = specVersion; }

    public int getTotalEndpoints() { return totalEndpoints; }
    public void setTotalEndpoints(int totalEndpoints) { this.totalEndpoints = totalEndpoints; }

    public int getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(int totalTestCases) { this.totalTestCases = totalTestCases; }

    public List<EndpointAnalysis> getEndpoints() { return endpoints; }
    public void setEndpoints(List<EndpointAnalysis> endpoints) { this.endpoints = endpoints; }
}
