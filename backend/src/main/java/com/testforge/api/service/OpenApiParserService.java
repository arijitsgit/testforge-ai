package com.testforge.api.service;

import com.testforge.api.model.EndpointInfo;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OpenApiParserService {

    public record ParsedSpec(String title, String version, List<EndpointInfo> endpoints) {}

    public ParsedSpec parse(String specContent) {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        SwaggerParseResult result = new OpenAPIV3Parser().readContents(specContent, null, options);
        if (result.getOpenAPI() == null) {
            throw new IllegalArgumentException("Invalid OpenAPI spec: " + String.join(", ", result.getMessages()));
        }

        OpenAPI openAPI = result.getOpenAPI();
        String title = openAPI.getInfo() != null ? openAPI.getInfo().getTitle() : "Unknown";
        String version = openAPI.getInfo() != null ? openAPI.getInfo().getVersion() : "Unknown";

        List<EndpointInfo> endpoints = new ArrayList<>();

        if (openAPI.getPaths() != null) {
            openAPI.getPaths().forEach((path, pathItem) -> {
                extractOperations(path, pathItem).forEach(endpoints::add);
            });
        }

        return new ParsedSpec(title, version, endpoints);
    }

    private List<EndpointInfo> extractOperations(String path, PathItem pathItem) {
        List<EndpointInfo> result = new ArrayList<>();

        Map<String, Operation> ops = Map.of(
            "GET",    pathItem.getGet()    != null ? pathItem.getGet()    : new Operation(),
            "POST",   pathItem.getPost()   != null ? pathItem.getPost()   : new Operation(),
            "PUT",    pathItem.getPut()    != null ? pathItem.getPut()    : new Operation(),
            "DELETE", pathItem.getDelete() != null ? pathItem.getDelete() : new Operation(),
            "PATCH",  pathItem.getPatch()  != null ? pathItem.getPatch()  : new Operation()
        );

        ops.forEach((method, op) -> {
            if (op.getOperationId() == null && op.getSummary() == null) return;

            EndpointInfo info = new EndpointInfo();
            info.setPath(path);
            info.setMethod(method);
            info.setSummary(op.getSummary());
            info.setDescription(op.getDescription());

            List<String> params = new ArrayList<>();
            if (op.getParameters() != null) {
                op.getParameters().forEach(p -> params.add(p.getIn() + ":" + p.getName() +
                        (Boolean.TRUE.equals(p.getRequired()) ? "*" : "")));
            }
            info.setParameters(params);

            if (op.getRequestBody() != null && op.getRequestBody().getContent() != null) {
                op.getRequestBody().getContent().forEach((mediaType, content) -> {
                    if (content.getSchema() != null) {
                        info.setRequestBodySchema(schemaToString(content.getSchema()));
                    }
                });
            }

            List<Integer> codes = new ArrayList<>();
            if (op.getResponses() != null) {
                op.getResponses().keySet().forEach(code -> {
                    try { codes.add(Integer.parseInt(code)); } catch (NumberFormatException ignored) {}
                });
            }
            info.setResponseCodes(codes);
            info.setRiskScore(computeRiskScore(method, path, params, codes));

            result.add(info);
        });

        return result;
    }

    private String schemaToString(Schema<?> schema) {
        if (schema == null) return "";
        StringBuilder sb = new StringBuilder();
        if (schema.getType() != null) sb.append("type:").append(schema.getType());
        if (schema.getProperties() != null) {
            sb.append(" properties:[");
            schema.getProperties().forEach((k, v) -> sb.append(k).append(","));
            sb.append("]");
        }
        return sb.toString();
    }

    private int computeRiskScore(String method, String path, List<String> params, List<Integer> codes) {
        int score = 5;
        if ("DELETE".equals(method)) score += 3;
        else if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) score += 2;
        if (path.contains("auth") || path.contains("payment") || path.contains("account")) score += 2;
        if (codes.stream().anyMatch(c -> c >= 400)) score += 1;
        return Math.min(score, 10);
    }
}
