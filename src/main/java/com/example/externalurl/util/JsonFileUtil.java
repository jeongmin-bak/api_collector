package com.example.externalurl.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class JsonFileUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void createJsonFile(String jsonFilePath, Map<String, Object> params) throws IOException {
        log.info("json 파일 경로 : {}", jsonFilePath);
        String srcUseType = "unload";
        String filePath = jsonFilePath + "/" + srcUseType + "_" + params.get("JB_ID") + ".json";
        Path path = Paths.get(filePath);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new File(filePath), params);
        log.info("배치 작업을 위한 JSON 파일이 성공적으로 생성되었습니다. 파일 경로: {}", filePath);
    }
}
