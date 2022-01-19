package com.lvonce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.lvonce.impl.DataRepositoryImpl;
import com.lvonce.impl.QueryLanguageMapperImpl;
import com.lvonce.impl.RequestHandlerImpl;
import com.lvonce.utils.ResultSetSerializer;
import io.undertow.Undertow;
import io.undertow.util.Headers;

public class App {
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final RequestHandler requestHandler = new RequestHandlerImpl();
    private static final QueryLanguageMapper languageMapper = new QueryLanguageMapperImpl();
    private static final DataRepository dataRepository = new DataRepositoryImpl();
    private static final FileHandler fileHandler = new FileHandler();
    static  {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ResultSetSerializer());
        jsonMapper.registerModule(module);
    }

    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(exchange -> {
                    try {
                        boolean isFileContent = fileHandler.handle(exchange);
                        if (isFileContent) {
                            return;
                        }

                        RequestHandler.RequestHandleResult handleResult = requestHandler.handle(exchange);
                        QueryLanguageMapper.QueryCommand queryCommand = languageMapper.generateQueryCommand(handleResult);
                        ObjectNode result;
                        if (handleResult.getMatch().getMethod().equalsIgnoreCase("GET")) {
                            result = dataRepository.executeQuery(queryCommand.getSource(), queryCommand.getCommand());
                        } else {
                            result = dataRepository.executeUpdate(queryCommand.getSource(), queryCommand.getCommand());
                        }
                        result.putIfAbsent("success", BooleanNode.getTrue());
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(jsonMapper.writeValueAsString(result));

                    } catch (RequestValidator.ValidateException ex) {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        ObjectNode result = jsonMapper.createObjectNode();
                        result.putIfAbsent("success", BooleanNode.getFalse());
                        result.putIfAbsent("errorCode", new TextNode(ex.getErrorCode()));
                        result.putIfAbsent("errorMsg", new TextNode(ex.getErrorMsg()));
                        exchange.getResponseSender().send(jsonMapper.writeValueAsString(result));
                    }
                }).build();
        server.start();
    }
}
