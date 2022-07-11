package com.lvonce.openapi.data;

import com.lvonce.openapi.data.utils.ResourceLoader;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@AllArgsConstructor
public class DefinitionHandler {

    RequestHandler requestHandler;

    boolean isDefinitionPath(String path) {
        log.info("path: {}", path);
        return "/definition".equals(path);
    }

    boolean handle(HttpServerExchange exchange) {
        log.info("definition handler");
        HttpString method = exchange.getRequestMethod();
        if (!method.equalToString("POST")) {
            return false;
        }

        String path = exchange.getRelativePath();
        if (!isDefinitionPath(path)) {
            return false;
        }

        exchange.getRequestReceiver().receiveFullString((HttpServerExchange serverExchange, String content)-> {
            log.info("yaml definition: {}", content);
            requestHandler.setYamlDefinition(content);
        });



        return true;
    }


}
