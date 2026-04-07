package com.example.externalurl.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
@Slf4j
public class DiscoveryReConnectUtil {
    private final RestTemplate restTemplate;

    public String restConnectWithRetryManager() {
        int maxRetries = 3;
        int retryDelay = 10 * 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            boolean forcedHaFlag = (attempt == maxRetries);
            String managerUrl = "{managerurl}";
            String restUrl = managerUrl + "/dp/restCheck";

            try {
                HttpEntity<String> requestEntity = new HttpEntity<>(null);
                ResponseEntity<String> response = restTemplate.exchange(restUrl, HttpMethod.GET, requestEntity, String.class);
                HttpStatusCode status = response.getStatusCode();

                if (status.is2xxSuccessful()) {
                    log.info("연결 성공! 응답 코드 : {}", status);
                    log.info("응답 내용 : {}", response.getBody());
                    return managerUrl;
                }

                log.warn("응답 코드: {} | 재시도 {}/{}", status, attempt, maxRetries);
            } catch (Exception e) {
                log.error("연결 실패 (시도 {}/{}): {}", attempt, maxRetries, e.getMessage(), e);
            }

            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("인터럽트 발생, 재시도 중단");
                    break;
                }
            }
        }
        log.error("최대 재시도 횟수 도달, 요청 실패");
        throw new RuntimeException("최대 재시도 횟수 도달, 요청 실패");
    }
}