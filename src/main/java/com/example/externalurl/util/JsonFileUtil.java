package com.example.externalurl.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class JsonFileUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void createJsonFile(String jsonFilePath, Map<String, Object> params) {
        log.info("json 파일 경로 : {}", jsonFilePath);

    }
}
