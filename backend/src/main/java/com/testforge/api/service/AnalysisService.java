package com.testforge.api.service;

import com.testforge.api.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalysisService {

    private final OpenApiParserService parserService;
    private final ClaudeService claudeService;

    public AnalysisService(OpenApiParserService parserService, ClaudeService claudeService) {
        this.parserService = parserService;
        this.claudeService = claudeService;
    }

    public AnalysisResult analyzeSpec(String specContent) {
        OpenApiParserService.ParsedSpec parsed = parserService.parse(specContent);
        List<EndpointInfo> endpoints = parsed.endpoints();

        // Single Claude call for all endpoints
        Map<String, List<TestCase>> allTestCases = claudeService.generateTestCasesForAll(endpoints);

        List<EndpointAnalysis> analyses = new ArrayList<>();
        int totalTestCases = 0;

        for (EndpointInfo endpoint : endpoints) {
            String key = endpoint.getMethod() + " " + endpoint.getPath();
            List<TestCase> testCases = allTestCases.getOrDefault(key, List.of());

            EndpointAnalysis analysis = new EndpointAnalysis();
            analysis.setEndpoint(endpoint);
            analysis.setTestCases(testCases);
            analysis.setRiskScore(endpoint.getRiskScore());
            analysis.setCoverageStatus("NONE");

            analyses.add(analysis);
            totalTestCases += testCases.size();
        }

        AnalysisResult result = new AnalysisResult();
        result.setSpecTitle(parsed.title());
        result.setSpecVersion(parsed.version());
        result.setTotalEndpoints(endpoints.size());
        result.setTotalTestCases(totalTestCases);
        result.setEndpoints(analyses);

        return result;
    }

    public CoverageGapResult analyzeCoverage(String specContent, String testCode) {
        OpenApiParserService.ParsedSpec parsed = parserService.parse(specContent);
        List<EndpointInfo> endpoints = parsed.endpoints();

        // Single Claude call for all endpoints
        Map<String, List<TestCase>> allGaps = claudeService.analyzeCoverageGapsForAll(endpoints, testCode);

        List<EndpointAnalysis> analyses = new ArrayList<>();
        int covered = 0, partial = 0, uncovered = 0;

        for (EndpointInfo endpoint : endpoints) {
            String key = endpoint.getMethod() + " " + endpoint.getPath();
            List<TestCase> gaps = allGaps.getOrDefault(key, List.of());

            EndpointAnalysis analysis = new EndpointAnalysis();
            analysis.setEndpoint(endpoint);
            analysis.setTestCases(gaps);
            analysis.setRiskScore(endpoint.getRiskScore());

            String status;
            if (gaps.isEmpty()) { status = "COVERED"; covered++; }
            else if (gaps.size() <= 2) { status = "PARTIAL"; partial++; }
            else { status = "NONE"; uncovered++; }

            analysis.setCoverageStatus(status);
            analyses.add(analysis);
        }

        int total = endpoints.size();
        CoverageGapResult result = new CoverageGapResult();
        result.setTotalEndpoints(total);
        result.setCoveredEndpoints(covered);
        result.setPartialEndpoints(partial);
        result.setUncoveredEndpoints(uncovered);
        result.setCoveragePercent(total > 0 ? (double) covered / total * 100 : 0);
        result.setEndpoints(analyses);

        return result;
    }
}
