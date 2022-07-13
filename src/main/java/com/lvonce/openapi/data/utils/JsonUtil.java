package com.lvonce.openapi.data.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex) {
            return "";
        }
    }
}
