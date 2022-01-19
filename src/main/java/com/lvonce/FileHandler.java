package com.lvonce;

import com.lvonce.utils.ResourceLoader;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class FileHandler {
    private String[] fileSuffixes = {".html", ".htm", ".js", ".css",
            ".json", ".yaml", ".yml",
            ".jpeg", ".jpg", ".png"};

    private boolean isFileSuffix(String path) {
        for (String suffix : fileSuffixes) {
            if (path.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    boolean handle(HttpServerExchange exchange) {
        HttpString method = exchange.getRequestMethod();
        if (!method.equalToString("GET")) {
            return false;
        }

        String path = exchange.getRelativePath();
        if (!isFileSuffix(path)) {
            return false;
        }

        try {
            String fileContent = ResourceLoader.loadContent("assets" + path);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
            exchange.getResponseSender().send(fileContent);
        } catch (IOException ex) {
            log.error("{}", ex.getMessage());
        }
        return true;
    }
}
