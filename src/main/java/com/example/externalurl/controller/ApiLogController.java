package com.example.externalurl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/v1/batch/external")
@RequiredArgsConstructor
public class ApiLogController {
    @Value("${agnet.batch.logs.dir_path}")
    private String LOG_DIRECTORY;

    @PostMapping("/log")
    public ResponseEntity<List<String>> getLogs(@RequestBody Map<String, Object> requestBody) {
        String jbId = requestBody.get("JB_ID").toString();
        String jbDt = requestBody.get("JB_DT").toString();

        try {
            List<String> logContent;
            Path logFilePath = Paths.get("LOG_DIRECTORY" + "/unload/" + "api_collector" + "_" + jbId + "_" + jbDt + ".log");
            log.info("로그 파일 경로 : {}", logFilePath);
            if (Files.exists(logFilePath)) {
                try (Stream<String> lines = Files.lines(logFilePath, StandardCharsets.UTF_8)) {
                    logContent = lines.map(line -> line.replaceAll("\\u001B\\[[;\\d]*m", "").replaceAll("\\t",""))
                        .map(line -> line + "\n")
                        .collect(Collectors.toList());
                }
            } else {
                logContent = Collections.singletonList("Log file not found for job ID :" + jbId);
            }
            return ResponseEntity.ok(logContent);
        } catch (Exception e){
            String errorMessage = "Error fetching logs for job ID: " + jbId + " - " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList(errorMessage));
        }
    }


}
