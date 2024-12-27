package com.example.externalurl.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class DynamicUrlBuilder {
    /**
     * Meta 정보를 기반으로 URL 생성
     *
     * @param baseUrl     기본 URL
     * @param pathParams  Path Parameter 리스트
     * @param queryParams Query Parameter 맵
     * @return 동적으로 생성된 URL
     */
    public static String buildUrl(String baseUrl, List<String> pathParams, Map<String, String> queryParams){
        StringBuilder urlBuilder = new StringBuilder(baseUrl);

        // Path Parameters 추가
        if (pathParams != null && !pathParams.isEmpty()) {
            urlBuilder.append("/").append(String.join("/", pathParams));
        }

        // Query Parameters 추가
        if (queryParams != null && !queryParams.isEmpty()) {
            urlBuilder.append("?");
            queryParams.forEach((key, value) -> {
                if ((key != null && !key.isEmpty()) && (value != null)) {
                }
            });

            // 마지막 '&' 제거
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        return urlBuilder.toString();

    }

    /**
     * 쿼리 매개변수 값을 URL 인코딩
     *
     * @param keyValue 쿼리 매개변수 값
     * @return 인코딩된 값
     */
    private static String encodeKeyValue(String keyValue) {
        try {
            return URLEncoder.encode(keyValue, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding query parameter value: " + keyValue, e);
        }
    }
}
