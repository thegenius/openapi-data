package com.lvonce.openapi.data;

import com.lvonce.openapi.data.utils.JsonUtil;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TableHandler {
    @Data
    @AllArgsConstructor
    static class Field {
        String fieldName;
        String fieldType;
    }

    @Data
    static class Table {
        List<Field> fieldList;
    }

    boolean handle(HttpServerExchange exchange) {
        log.info("definition handler");
        HttpString method = exchange.getRequestMethod();
        if (!method.equalToString("GET")) {
            return false;
        }

        String path = exchange.getRelativePath();
        if (!path.startsWith("/table/")) {
            return false;
        }

        Field nameField = new Field("username", "String");
        Field passwordField = new Field("password", "String");
        Table table = new Table();
        table.fieldList = new ArrayList<>();
        table.fieldList.add(nameField);
        table.fieldList.add(passwordField);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        String tableJson = JsonUtil.toJson(table);
        exchange.getResponseSender().send(tableJson);
        return true;
    }

}
