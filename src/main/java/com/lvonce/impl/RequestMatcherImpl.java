package com.lvonce.impl;

import com.lvonce.RequestMatcher;
import com.lvonce.utils.AntPathMatcher;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RequestMatcherImpl implements RequestMatcher {
    private static AntPathMatcher antPathMatcher = new AntPathMatcher();

    private Operation matchOperation(PathItem pathItem, HttpServerExchange exchange) {
        HttpString method = exchange.getRequestMethod();
        if (method.equalToString("GET")) {
            return pathItem.getGet();
        }
        if (method.equalToString("POST")) {
            return pathItem.getPost();
        }
        if (method.equalToString("PUT")) {
            return pathItem.getPut();
        }
        if (method.equalToString("PATCH")) {
            return pathItem.getPatch();
        }
        if (method.equalToString("DELETE")) {
            return pathItem.getDelete();
        }
        return null;
    }

    RequestMatchResult matchPattern(OpenAPI openAPI, HttpServerExchange exchange, String pathPattern, String requestPath) {
        try {
            if (!antPathMatcher.isPattern(pathPattern)) {
                return RequestMatchResult.ofFailure();
            }

            Map<String, String> pathVariables = antPathMatcher.extractUriTemplateVariables(pathPattern, requestPath);
            PathItem pathItem = openAPI.getPaths().get(pathPattern);
            Operation operation = matchOperation(pathItem, exchange);
            String method = exchange.getRequestMethod().toString();
            return RequestMatchResult.ofSuccess(pathPattern, method, operation, pathVariables);
        } catch (Exception ex) {
            log.debug("{}", ex.getMessage());
            return RequestMatchResult.ofFailure();
        }
    }

    @Override
    public RequestMatchResult match(OpenAPI openAPI, HttpServerExchange exchange) {
        String requestPath = exchange.getRequestPath();
        PathItem pathItem = openAPI.getPaths().get(requestPath);
        if (pathItem != null) {
            log.debug("request path {} match exactly", requestPath);
            Operation operation = matchOperation(pathItem, exchange);
            String method = exchange.getRequestMethod().toString();
            return RequestMatchResult.ofSuccess(requestPath, method, operation);
        }

        for (String pathPattern : openAPI.getPaths().keySet()) {
            RequestMatchResult matchResult = matchPattern(openAPI, exchange, pathPattern, requestPath);
            if (matchResult.isSuccess()) {
                log.debug("request path {} match pattern {}", requestPath, pathPattern);
                return matchResult;
            }
        }

        log.debug("request path {} not match any path", requestPath);
        return RequestMatchResult.ofFailure();
    }
}
