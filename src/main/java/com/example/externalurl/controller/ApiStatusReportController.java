package com.example.externalurl.controller;


import com.example.externalurl.util.JobManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/batch/status/external")
@Slf4j
public class ApiStatusReportController {
    private final RestTemplate restTemplate;
    //private final DiscoveryReConnectUtil discoveryReConnectUtil;

    //@Value("")
    // 임시
    // @Value("${BATCH_META_FILE_PATH}")
    private String jsonFilePath;

    public ApiStatusReportController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/updateBatch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateBatch(@RequestBody Map<String, String> reqExecBatch) {
        log.info("외부데이터 수집 작업 상태 업데이터 요청 파라미터 : {}", reqExecBatch);
        return processBatchStatus(reqExecBatch, "/dp/external/status/update");
    }

    @PostMapping(value = "/completeBatch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> completeBatch(@RequestBody Map<String, String> reqExecBatch) {
        log.info("외부데이터 완료 요청 파라미터 : {}", reqExecBatch);
        return processBatchStatus(reqExecBatch, "/dp/external/complete");
    }

    @PostMapping(value = "/releaseConnection", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void releaseConnection(@RequestBody Map<String, String> reqExecBatch) {
        String jobId = reqExecBatch.get("JB_ID");
        if (JobManager.getJavaJobInfo(jobId)) {
            Map<String, Object> removeJob = JobManager.removeJob(jobId);
            try {
                log.info("정지요청을 받은 작업의 메타 파일 삭제 : {}", removeJob.get("JSON_FILE_NAME"));
                Files.deleteIfExists(Path.of((String) removeJob.get("JSON_FILE_NAME")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log.info("정지요청을 받은 작업의 데이터 파일 삭제 : {}", removeJob.get("TMP_DIR"));
            Path rootPath = Paths.get((String) removeJob.get("TMP_DIR"));

            if (!Files.exists(rootPath)) {
                log.warn("지정된 경로가 존재하지 않습니다: {}", rootPath);
                return;
            }
        } else {
            log.error("{} 작업은 실행중인 상태가 아닙니다.", jobId);
            return;
        }
        log.info("{} 작업 정지가 완료되었습니다.", jobId);
    }

    private ResponseEntity<String> processBatchStatus(Map<String, String> reqExecBatch, String endpoint) {
        String jobId = reqExecBatch.get("JB_ID");
        String status = reqExecBatch.get("EXE_STSC");
        String message = reqExecBatch.get("ERR_LOG");
        String exeJbRng = reqExecBatch.get("EXE_JB_RNG");

        String restUrl = "{managerUrl}";
        log.info("외부데이터 수집 처리 상태 업데이터 : Job ID: {}, 상태 코드 : {}, 작업 : {}", jobId, status, exeJbRng);
        return sendStatusToSchedulerManager(restUrl, jobId, status, message, exeJbRng, endpoint);
    }

    private ResponseEntity<String> sendStatusToSchedulerManager(String restUrl, String jobId, String status, String message, String exeJbRng, String managerEndpoint) {
        String jobMetaFile;
        jobMetaFile = jsonFilePath + "/api_unload_" + jobId + ".json";

        if("99".equals(status) || "10".equals(status)) {
            try {
                JobManager.removeJob(jobId);
                log.info("{} 메타 정보 파일 제거 ", jobMetaFile);
                Files.deleteIfExists(Path.of(jobMetaFile));
            } catch (NoSuchElementException | IOException e) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, Object> payload = new HashMap<>();
                payload.put("JB_ID", jobId);
                payload.put("EXE_STSC", "99");
                payload.put("EXE_JB_RNG", exeJbRng);
                payload.put("ERR_LOG", "Agent 에서 관리되지 않는 작업입니다. : " + e.getMessage());
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
                restTemplate.postForEntity(restUrl + managerEndpoint, requestEntity, Void.class);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = new HashMap<>();
        payload.put("JB_ID", jobId);
        payload.put("EXE_STSC", status);
        if (message != null && !message.isEmpty()) {
            payload.put("ERR_LOG", message);
        }
        payload.put("EXE_JB_RNG", exeJbRng);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        log.info("Rest URL : {}", restUrl + managerEndpoint);
        log.info("Scheduler Manager 로 상태 전송 URL : {} ", restUrl + managerEndpoint);
        restTemplate.postForEntity(restUrl + managerEndpoint, requestEntity, Void.class);
        return ResponseEntity.ok("Status successfully sent to Scheduler Manager");
    }


}
