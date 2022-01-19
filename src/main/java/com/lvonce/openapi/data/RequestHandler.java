package com.lvonce.openapi.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.undertow.server.HttpServerExchange;
import lombok.Data;

public interface RequestHandler {
    @Data
    class RequestHandleResult {
        private RequestMatcher.RequestMatchResult match;
        private ObjectNode context;

        public static RequestHandleResult of(RequestMatcher.RequestMatchResult matchResult, ObjectNode context) {
            RequestHandleResult result = new RequestHandleResult();
            result.match = matchResult;
            result.context = context;
            return result;
        }
    }

    RequestHandleResult handle(HttpServerExchange exchange) throws RequestValidator.ValidateException;
}
