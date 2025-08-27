package com.example.externalurl.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SendStatus {

    @Value("${server.agent.url}")
    private String agentUrl;

    @Value("${server.agent.port}")
    private String agentPort;

    private final RestTemplate restTemplate;

    public SendStatus(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    public void sendStatusUpdate(String jobId, String status, String errMsg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("JB_ID", jobId);
        payload.put("EXE_STSC", status);
        if (errMsg != null && !errMsg.isEmpty()) {
            payload.put("ERR_LOG", errMsg);
        }
        payload.put("EXE_JB_RNG", "02");
        String restUrl = "http://" + agentUrl + ":" + agentPort + "/v1/batch/status/external/updateBatch";
        log.info("Agent Url: {}", restUrl);
        log.info("외부데이터 수집 작업 상태 업데이트: {}", payload);

        try {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(restUrl, requestEntity, Void.class);
        } catch (Exception e) {
            log.error("{}로 상태 업데이트 전송 실패 : {}", restUrl, e.getMessage());
        }
    }

    public void sendCompleteStatusUpdate(String jobId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("JB_ID", jobId);
        payload.put("EXE_STSC", "10");
        payload.put("EXE_JB_RNG", "02");

        String restUrl = "http://" + agentUrl + ":" + agentPort + "/v1/batch/status/external/completeBatch";

        log.info("Manager Url : {}", restUrl);
        log.info("완료 상태 업데이트 : {}", payload);

        try {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(restUrl, requestEntity, Void.class);
        } catch (Exception e) {
            log.error("{}에 완료 상태 업데이트 전송 실패 : {}", restUrl, e.getMessage());
        }
    }

    public void sendStatusReleaseConnection(String jobId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = new HashMap<>();
        payload.put("JB_ID", jobId);
        payload.put("SRC_TYPE", "01");
        payload.put("EXE_JB_RNG", "02");
        String restUrl = "http://" + agentUrl + ":" + agentPort + "/v1/batch/status/external/releaseConnection";
        log.info("외부데이터 수집 작업 자원정리 요청 신호 전송");
        try {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(restUrl, requestEntity, Void.class);
        } catch (Exception e) {
            log.error("{}로 리소스 해제 요청 전송 실패: {}", restUrl, e.getMessage());
        }
    }
}