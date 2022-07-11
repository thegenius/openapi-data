package com.lvonce.openapi.data.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lvonce.openapi.data.RequestHandler;
import com.lvonce.openapi.data.RequestMatcher;
import com.lvonce.openapi.data.RequestValidator;
import com.lvonce.openapi.data.utils.ResourceLoader;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.undertow.server.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestHandlerImpl implements RequestHandler {
    private static RequestMatcher matcher = new RequestMatcherImpl();
    private static RequestValidator validator = new RequestValidatorImpl();
    private OpenAPI openAPI;

    public RequestHandlerImpl() {
        try {
            String openApiContent = ResourceLoader.loadContent("openapi.yaml");
            SwaggerParseResult result = new OpenAPIParser().readContents(openApiContent, null , null);
            this.openAPI = result.getOpenAPI();
        } catch (Exception ex) {
            this.openAPI = null;
        }
    }

    public void setYamlDefinition(String yamlDefinition) {
        try {
            SwaggerParseResult result = new OpenAPIParser().readContents(yamlDefinition, null , null);
            this.openAPI = result.getOpenAPI();
        } catch (Exception ex) {
            this.openAPI = null;
        }
    }




    @Override
    public RequestHandleResult handle(HttpServerExchange exchange) throws RequestValidator.ValidateException {
        RequestMatcher.RequestMatchResult matchResult = matcher.match(openAPI, exchange);
        if (matchResult.isSuccess()) {
            ObjectNode context = validator.validate(matchResult, exchange);
            return RequestHandleResult.of(matchResult, context);
        } else {
            throw new RequestValidator.ValidateException("not match", "");
        }
    }
}
