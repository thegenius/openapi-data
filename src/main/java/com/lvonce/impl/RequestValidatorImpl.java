package com.lvonce.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.lvonce.RequestMatcher;
import com.lvonce.RequestValidator;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import lombok.extern.slf4j.Slf4j;

import java.util.Deque;
import java.util.List;
import java.util.Map;

@Slf4j
public class RequestValidatorImpl implements RequestValidator{
    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    static {
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private JsonNode tryParseWithWrap(String content) {
        try {
            content = "\"" + content + "\"";
            return jsonMapper.readTree(content);
        } catch (Exception ex) {
            return null;
        }
    }

    private JsonNode parseToNode(String content) {
        try {
            return jsonMapper.readTree(content);
        } catch (Exception ex) {
            return tryParseWithWrap(content);
        }
    }

    private ObjectNode fillObjectNode(ObjectNode node, String in) {
        ObjectNode sourceNode = (ObjectNode) node.get(in);
        if (sourceNode == null) {
            sourceNode = jsonMapper.createObjectNode();
            node.putIfAbsent(in, sourceNode);
        }
        return sourceNode;
    }

    private void fillObjectNode(ObjectNode node, String in, String name, JsonNode value) {
        ObjectNode sourceNode = fillObjectNode(node, in);
        sourceNode.putIfAbsent(name, value);
    }

    private JsonNode extractParam(ObjectNode node, Parameter param, HttpServerExchange exchange,
                                  Map<String, String> pathVariables) throws RequestValidator.ValidateException {
        try {
            String in = param.getIn();
            String paramName = param.getName();
            if (in.equalsIgnoreCase("query")) {
                Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
                Deque<String> paramValue = queryParams.get(paramName);
                JsonNode paramNode = parseToNode(paramValue.getFirst());
                fillObjectNode(node, in, paramName, paramNode);
                return paramNode;
            }

            if (in.equalsIgnoreCase("path")) {
                String pathVariable = pathVariables.get(paramName);
                JsonNode paramNode = parseToNode(pathVariable);
                fillObjectNode(node, in, paramName, paramNode);
                return paramNode;
            }

            if (in.equalsIgnoreCase("cookie")) {
                Map<String, Cookie> cookies = exchange.getRequestCookies();
                Cookie cookie = cookies.get(paramName);
                JsonNode paramNode = parseToNode(cookie.getValue());
                fillObjectNode(node, in, paramName, paramNode);
                return paramNode;
            }

            if (in.equalsIgnoreCase("header")) {
                HeaderMap headerMap = exchange.getRequestHeaders();
                HeaderValues headerValues = headerMap.get(paramName);
                JsonNode paramNode = parseToNode(headerValues.getFirst());
                fillObjectNode(node, in, paramName, paramNode);
                return paramNode;
            }
            return null;

        } catch (Exception ex) {
            throw new RequestValidator.ValidateException("extract param fail", param.getName() + " extract fail");
        }

    }


    private JsonSchema buildJsonSchema(Parameter param) throws RequestValidator.ValidateException {
        try {
            String schemaJsonStr = jsonMapper.writeValueAsString(param.getSchema());
            log.info("schema: {}", schemaJsonStr);
            JsonNode jsonNode = jsonMapper.readTree(schemaJsonStr);
            return factory.getJsonSchema(jsonNode);
        } catch (Exception ex) {
            throw new RequestValidator.ValidateException("schema not valid", "");
        }
    }

    private String buildErrorMsg(ProcessingReport report) {
        for (ProcessingMessage msg : report) {
            if (msg.getLogLevel().equals(LogLevel.ERROR)) {
                return msg.getMessage();
            }
        }
        return "";
    }

    private ProcessingReport validateParam(Parameter param, JsonSchema jsonSchema, JsonNode paramNode) throws RequestValidator.ValidateException {
        ProcessingReport report;
        try {
            report = jsonSchema.validate(paramNode);
        } catch (Exception ex) {
            throw new RequestValidator.ValidateException("validate error", ex.getMessage());
        }

        if (report.isSuccess()) {
            log.info("validate success");
        } else {
            throw new RequestValidator.ValidateException("validate error", param.getName() +":" +buildErrorMsg(report));
        }
        return report;
    }

    @Override
    public ObjectNode validate(RequestMatcher.RequestMatchResult matchResult, HttpServerExchange exchange) throws ValidateException {
        List<Parameter> paramList = matchResult.getOperation().getParameters();
        Map<String, String> pathVariables = matchResult.getPathVariables();
        ObjectNode jsonNodeResult = jsonMapper.createObjectNode();
        for (Parameter param: paramList) {
            JsonSchema jsonSchema = buildJsonSchema(param);
            JsonNode paramNode = extractParam(jsonNodeResult, param, exchange, pathVariables);
            validateParam(param, jsonSchema, paramNode);
        }
        return jsonNodeResult;
    }
}
