package com.testforge.api.model;

import java.util.List;

public class CoverageGapResult {
    private int totalEndpoints;
    private int coveredEndpoints;
    private int partialEndpoints;
    private int uncoveredEndpoints;
    private double coveragePercent;
    private List<EndpointAnalysis> endpoints;

    public CoverageGapResult() {}

    public int getTotalEndpoints() { return totalEndpoints; }
    public void setTotalEndpoints(int totalEndpoints) { this.totalEndpoints = totalEndpoints; }

    public int getCoveredEndpoints() { return coveredEndpoints; }
    public void setCoveredEndpoints(int coveredEndpoints) { this.coveredEndpoints = coveredEndpoints; }

    public int getPartialEndpoints() { return partialEndpoints; }
    public void setPartialEndpoints(int partialEndpoints) { this.partialEndpoints = partialEndpoints; }

    public int getUncoveredEndpoints() { return uncoveredEndpoints; }
    public void setUncoveredEndpoints(int uncoveredEndpoints) { this.uncoveredEndpoints = uncoveredEndpoints; }

    public double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }

    public List<EndpointAnalysis> getEndpoints() { return endpoints; }
    public void setEndpoints(List<EndpointAnalysis> endpoints) { this.endpoints = endpoints; }
}
