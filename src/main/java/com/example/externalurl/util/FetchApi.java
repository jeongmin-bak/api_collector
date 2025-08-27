package com.example.externalurl.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FetchApi {
    public static String fetchApiData(String apiUrl, String dataFormat) {
        int MAX_RETRIES = 5;
        ObjectMapper objectMapper = new ObjectMapper();
        String response = "";
        boolean successCheck = false;

        try {
            URL url = new URL(apiUrl);
            BufferedReader br;
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-type", String.format("application/%s", dataFormat));
                    conn.setConnectTimeout(600000);
                    conn.setReadTimeout(600000);

                    String resContentType = conn.getHeaderField("Content-Type").toLowerCase();
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK || (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300)) {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        successCheck = true;
                        break;
                    }
                    if (conn.getResponseCode() == 504) {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        response = br.lines().collect(Collectors.joining());
                        successCheck = false;
                        log.error("응답값 에러 발생!! {}", response);

                        if (response.contains("fallback")) {
                            Map<String, String> tmpMap = objectMapper.readValue(response, Map.class);
                            if (tmpMap.get("message").contains(("fallback"))) {
                                log.info("재시도 요청 : {} fallback 문제로 다시 수집 요청", attempt);
                                response = "";
                                continue;
                            }
                        } else if(!resContentType.contains(dataFormat.toLowerCase())) {
                            log.info("응답값이 사용자 요청과 다른형식으로 응답되었습니다.");
                            response = "";
                        }
                        break;
                    }       
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if(conn != null) {
                        conn.disconnect();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (successCheck == false) {
            response = "RESPONSE_FAIL" + response;
            log.info("response fail fetchApiData : {}, success : {}", response, successCheck);
        }
        log.info("after fetchApiData 요약 : {}, success : {}", response, successCheck);
        return response;
    }
}