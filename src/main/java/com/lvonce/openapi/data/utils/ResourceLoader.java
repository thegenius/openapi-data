package com.lvonce.openapi.data.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {
    public static String loadContent(String resourcePath) throws IOException {
        InputStream inputStream = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("no input stream available");
        }
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}
