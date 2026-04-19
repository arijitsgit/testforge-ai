package com.testforge.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubService {

    private static final int MAX_FILES = 3;
    private static final int MAX_FILE_CHARS = 3000;
    private static final List<String> TEST_EXTENSIONS = List.of(".java", ".kt", ".groovy", ".py", ".ts", ".js");
    private static final List<String> TEST_KEYWORDS = List.of("test", "spec", "it_", "should");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String fetchTestPatterns(String githubUrl) {
        try {
            String[] ownerRepo = extractOwnerRepo(githubUrl);
            if (ownerRepo == null) return null;

            String owner = ownerRepo[0];
            String repo = ownerRepo[1];

            List<String> testFilePaths = fetchTestFilePaths(owner, repo);
            if (testFilePaths.isEmpty()) return null;

            StringBuilder combined = new StringBuilder();
            int fetched = 0;
            for (String path : testFilePaths) {
                if (fetched >= MAX_FILES) break;
                String content = fetchFileContent(owner, repo, path);
                if (content != null) {
                    combined.append("// File: ").append(path).append("\n");
                    combined.append(content, 0, Math.min(content.length(), MAX_FILE_CHARS));
                    combined.append("\n\n");
                    fetched++;
                }
            }
            return combined.length() > 0 ? combined.toString() : null;

        } catch (Exception e) {
            return null;
        }
    }

    private String[] extractOwnerRepo(String url) {
        Pattern pattern = Pattern.compile("github\\.com[:/]([^/]+)/([^/\\.]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2).replace(".git", "")};
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchTestFilePaths(String owner, String repo) {
        try {
            String url = "https://api.github.com/repos/%s/%s/git/trees/HEAD?recursive=1"
                    .formatted(owner, repo);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode tree = root.get("tree");

            List<String> testFiles = new ArrayList<>();
            if (tree != null) {
                tree.forEach(node -> {
                    String path = node.get("path").asText();
                    String type = node.get("type").asText();
                    if ("blob".equals(type) && isTestFile(path)) {
                        testFiles.add(path);
                    }
                });
            }
            return testFiles;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String fetchFileContent(String owner, String repo, String path) {
        try {
            String url = "https://raw.githubusercontent.com/%s/%s/HEAD/%s"
                    .formatted(owner, repo, path);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTestFile(String path) {
        String lower = path.toLowerCase();
        boolean hasTestExtension = TEST_EXTENSIONS.stream().anyMatch(lower::endsWith);
        boolean hasTestKeyword = TEST_KEYWORDS.stream().anyMatch(lower::contains);
        return hasTestExtension && hasTestKeyword;
    }
}
