package com.lvonce.openapi.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lvonce.openapi.data.QueryLanguageMapper;
import com.lvonce.openapi.data.RequestHandler;
import com.lvonce.openapi.data.RequestMatcher;
import com.lvonce.openapi.data.utils.JsonAwareObjectWrapper;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.*;
import lombok.Data;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class QueryLanguageMapperImpl implements QueryLanguageMapper {
    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static StringTemplateLoader stringLoader = new StringTemplateLoader();
    private static Configuration cfg = new Configuration(new Version("2.3.23"));
    static {
        cfg.setClassForTemplateLoading(QueryLanguageMapperImpl.class, "/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateLoader(stringLoader);
    }

    @Data
    static class Endpoint {
        private String url;
        private String method;

        public static Endpoint of(String url, String method) {
            Endpoint endpoint = new Endpoint();
            endpoint.url = url;
            endpoint.method = method;
            return endpoint;
        }

        public String toJson() {
            try {
                return jsonMapper.writeValueAsString(this);
            } catch (Exception ex) {
                return "";
            }
        }
    }

    private Template getTemplate(Endpoint endpoint) {
        try {
            String templateKey = endpoint.toJson();
            return cfg.getTemplate(templateKey);
        } catch (Exception ex) {
            return null;
        }
    }

    private void putTemplate(RequestMatcher.RequestMatchResult matchResult, QueryLanguageMapper.QueryCommand queryCommand) {
        String pathPattern = matchResult.getPathPattern();
        String method = matchResult.getMethod();
        stringLoader.putTemplate(Endpoint.of(pathPattern, method).toJson(), queryCommand.getCommand());
    }

    public QueryLanguageMapper.QueryCommand extractQueryCommand(RequestMatcher.RequestMatchResult matchResult) {
        try {
            Map<String, Object> extensions = matchResult.getOperation().getExtensions();
            if (extensions == null || extensions.isEmpty()) {
                return null;
            }
            Map<String, String> dataCommand = (Map<String, String>)extensions.get("x-data-command");
            String dataSourceType = dataCommand.get("type");
            String source = dataCommand.get("source");
            String command = dataCommand.get("command");
            return QueryLanguageMapper.QueryCommand.of(dataSourceType, source, command);
        } catch (Exception ex) {
            return null;
        }
    }


    @Override
    public QueryCommand generateQueryCommand(RequestHandler.RequestHandleResult handleResult) throws QueryLanguageException {
        QueryCommand queryCommand = extractQueryCommand(handleResult.getMatch());
        if (queryCommand == null) {
            return null;
        }

        putTemplate(handleResult.getMatch(), queryCommand);
        RequestMatcher.RequestMatchResult matchResult = handleResult.getMatch();
        String pathPattern = matchResult.getPathPattern();
        String method = matchResult.getMethod();
        Template template = getTemplate(Endpoint.of(pathPattern, method));
        if (template == null) {
            throw new QueryLanguageException("query template not found", pathPattern + "" + method + "query template not found");
        }

        ObjectNode context = handleResult.getContext();
        JsonAwareObjectWrapper objectWrapper = new JsonAwareObjectWrapper(new Version("2.3.23"));
        SimpleHash model = new SimpleHash(objectWrapper);
        model.put("context", context);

        try (StringWriter out = new StringWriter()) {
            template.process(model, out);
            queryCommand.setCommand(out.toString());
            return queryCommand;
        } catch (TemplateException tex) {
            throw new QueryLanguageException("query processing error",
                    pathPattern +" " + method + "query processing error");
        } catch (IOException ex) {
            throw new QueryLanguageException("query processing error",
                    pathPattern +" " + method + "template not found");
        }

    }
}
