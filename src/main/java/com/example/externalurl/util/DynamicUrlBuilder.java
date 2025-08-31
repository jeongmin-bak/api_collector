package com.example.externalurl.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DynamicUrlBuilder {
    public static String buildUrl(String apiUrl, Map<String, Object> urlParam, boolean isPath, String reqUrlType, Map<String, Object> bsInfoMap) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl);
        if (isPath && reqUrlType.equals("01")) {
            // path URL && 개별 URL로 요청시 날짜값을 동적으로 바꿔주는 분기문
            String[] pathArray = urlParam.get("URL_PATH").toString().split("/");
            if(bsInfoMap.get("BSDT_KEY") != null && bsInfoMap.get("BSDT_ORDER") != null) {
                // 날짜관련 키가 Null이 아니거나 날짜 관련 파라미터 순서가 있는경우
                // path 파라미터는 순서가 있기 때문에 날짜관련 파라미터 순서가 몇번인지 값을 넘겨주도록 설계
                // 첫번째가 공백이라 +1 필요
                int bsDtParamOrder = Integer.parseInt(bsInfoMap.get("BSDT_ORDER").toString()) + 1;
                String baseBsDt = pathArray[bsDtParamOrder];
                if (baseBsDt.matches("\\d{4}[A-Z]{1,2}\\d{1}")) {
                    // 특정 API인 경우 Q + 2025Q01 로 날짜값이 설정되어있어 문자가 들어간 경우 특정 메소드를 타야함.
                    String changeDateBsDt = DateUtil.changeBasicDateToStrDate(baseBsDt, bsInfoMap.get("BSDT").toString());
                    log.info("분기, 반기, 반월로 날짜포맷 변경을 시작합니다. {} -> 변경된 날짜포맷 : {}", bsInfoMap.get("BSDT").toString(), changeDateBsDt);
                    pathArray[bsDtParamOrder] = changeDateBsDt;
                } else {
                    String basicFormat = DateUtil.checkDateFormat(baseBsDt);
                    String formatterBsDt = DateUtil.calculate(bsInfoMap.get("BSDT").toString(), "", basicFormat);
                    log.info("{} -> 변경된 날짜포맷 : {}", basicFormat, formatterBsDt);
                    pathArray[bsDtParamOrder] = formatterBsDt;
                }
                return apiUrl + String.join("/", pathArray);
            } else if (isPath && reqUrlType.equals("02")){
                // PATH 형식 &&  파라미터 방식
                if (bsInfoMap.get("BSDT_KEY") != null) {
                    urlParam.forEach((key, value) -> {
                        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                        String encodedValue = URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
                        if (!key.isEmpty() && !(value.equals(""))) {
                            if (encodedKey.equals(bsInfoMap.get("BSDT_KEY").toString())) {
                                if (encodedValue.matches("\\d{4}[A-Z]{1,2}\\d{1}")) {
                                    // 특정 API인 경우 Q + 2025Q01 로 날짜값이 설정되어있어 문자가 들어간 경우 특정 메소드를 타야함.
                                    String changeDateBsDt = DateUtil.changeBasicDateToStrDate(value.toString(), bsInfoMap.get("BSDT").toString());
                                    log.info("분기, 반기, 반월로 날짜포맷 변경을 시작합니다. {} -> 변경된 날짜포맷 : {}", bsInfoMap.get("BSDT").toString(), changeDateBsDt);
                                    urlParam.put(key, changeDateBsDt);
                                } else {
                                    String basicFormat = DateUtil.checkDateFormat(value.toString());
                                    String formatterBsDt = DateUtil.calculate(bsInfoMap.get("BSDT").toString(), "", basicFormat);
                                    log.info("{} -> 변경된 날짜포맷 : {}", basicFormat, formatterBsDt);
                                    urlParam.put(key, formatterBsDt);
                                }
                            }
                        }
                    });
                } else {
                    return builder
                        .buildAndExpand(urlParam)
                        .toUriString();
                }
            }
            return UriComponentsBuilder.fromUriString(apiUrl)
                .buildAndExpand(urlParam)
                .toUriString();
        } else if ((isPath == false)) {
            // Query인 형식으로 요청이 온 경우
            urlParam.forEach((key, value) -> {
                if (!key.isEmpty() && !(value.equals(""))) {
                    String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                    String encodedValue = URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
                    if (bsInfoMap.get("BSDT_KEY") == null) {
                        log.info("날짜 변경 안함!");
                        builder.queryParam(encodedKey, encodedValue);
                    } else if (bsInfoMap.get("BSDT_KEY") != null && encodedKey.equals(URLEncoder.encode(bsInfoMap.get("BSDT_KEY").toString(), StandardCharsets.UTF_8))) {
                        String basicFormat = DateUtil.checkDateFormat(encodedValue);
                        String formatterBsDt = DateUtil.calculate(bsInfoMap.get("BSDT").toString(), "", basicFormat);
                        log.info("변경된 날짜포맷 : {}", formatterBsDt);
                        builder.queryParam(encodedKey, encodedValue);
                    } else {
                        builder.queryParam(encodedKey, encodedValue);
                    }
                }
            });
            return builder.build(false).toUriString();
        }
        return "";
    }
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

    public static String removeServiceKey(String baseUrl) {
        String regex = "(?!)[?&]([a-zA-Z0-9]*Key)=([^&]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(baseUrl);
        String key = "";
        String value = "";

        while (matcher.find()) {
            key = matcher.group(1);
            value = matcher.group(2);
        }
        String removeTokenUrl = baseUrl.replace(key + "=" + value, "");
        return removeTokenUrl;
    }
}
