package com.lvonce.openapi.data;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.undertow.server.HttpServerExchange;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

public interface RequestMatcher {
    @Data
    class RequestMatchResult {
        boolean success;
        String pathPattern;
        String method;
        Operation operation;
        Map<String, String> pathVariables;

        public static RequestMatchResult ofFailure() {
            RequestMatchResult result = new RequestMatchResult();
            result.success = false;
            result.operation = null;
            result.pathVariables = null;
            result.pathPattern = null;
            result.method = null;
            return result;
        }

        public static RequestMatchResult ofSuccess(String pathPattern, String method, Operation operation) {
            RequestMatchResult result = new RequestMatchResult();
            result.success = true;
            result.operation = operation;
            result.pathVariables = new HashMap<>();
            result.pathPattern = pathPattern;
            result.method = method;
            return result;
        }

        public static RequestMatchResult ofSuccess(String pathPattern, String method, Operation operation, Map<String, String> pathVariables) {
            RequestMatchResult result = new RequestMatchResult();
            result.success = true;
            result.operation = operation;
            result.pathVariables = pathVariables;
            result.pathPattern = pathPattern;
            result.method = method;
            return result;
        }
    }

    RequestMatchResult match(OpenAPI openAPI, HttpServerExchange exchange);

}
