package com.example.externalurl.service;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;

    private String restUrl;

    @PostConstruct
    public void init() {
        //restUrl = discoveryClient.getEngineManagerUrl() + "/dp/status/update";
        restUrl = "{managerUrl}" + "/dp/status/update";
    }

    public void notifyFailure(String message, String jobId, String srcUseType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("JB_ID", jobId);
        payload.put("EXE_STSC", "99");
        payload.put("ERR_LOG", message);
        payload.put("EXE_JB_RNG", srcUseType.equals("01") ? "02" : "04");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        restTemplate.postForObject(restUrl, requestEntity, Void.class);
    }
}