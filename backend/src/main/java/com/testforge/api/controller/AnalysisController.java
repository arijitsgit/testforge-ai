package com.testforge.api.controller;

import com.testforge.api.model.AnalysisResult;
import com.testforge.api.model.CoverageGapResult;
import com.testforge.api.model.TestCase;
import com.testforge.api.service.AnalysisService;
import com.testforge.api.service.ClaudeService;
import com.testforge.api.service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ClaudeService claudeService;
    private final GitHubService gitHubService;

    public AnalysisController(AnalysisService analysisService, ClaudeService claudeService, GitHubService gitHubService) {
        this.analysisService = analysisService;
        this.claudeService = claudeService;
        this.gitHubService = gitHubService;
    }

    @PostMapping("/analyze/spec")
    public ResponseEntity<?> analyzeSpec(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            AnalysisResult result = analysisService.analyzeSpec(content);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    @PostMapping("/analyze/coverage")
    public ResponseEntity<?> analyzeCoverage(
            @RequestParam("spec") MultipartFile specFile,
            @RequestParam("tests") MultipartFile testFile) {
        try {
            String specContent = new String(specFile.getBytes(), StandardCharsets.UTF_8);
            String testContent = new String(testFile.getBytes(), StandardCharsets.UTF_8);
            CoverageGapResult result = analysisService.analyzeCoverage(specContent, testContent);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    @PostMapping("/generate/test-code")
    public ResponseEntity<Map<String, String>> generateTestCode(
            @RequestParam("testCase") String testCaseJson,
            @RequestParam(defaultValue = "http://localhost:8080") String baseUrl,
            @RequestParam(value = "pattern", required = false) MultipartFile patternFile,
            @RequestParam(value = "githubUrl", required = false) String githubUrl) {
        try {
            TestCase testCase = new com.fasterxml.jackson.databind.ObjectMapper().readValue(testCaseJson, TestCase.class);
            String patternCode = null;
            if (githubUrl != null && !githubUrl.isBlank()) {
                patternCode = gitHubService.fetchTestPatterns(githubUrl);
            } else if (patternFile != null) {
                patternCode = new String(patternFile.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            String code = claudeService.generateJUnitCode(testCase, baseUrl, patternCode);
            return ResponseEntity.ok(Map.of("code", code));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "TestForge AI"));
    }
}
