package com.example.externalurl.handler;

import com.example.externalurl.component.RawApiDataLoader;
import com.example.externalurl.util.DynamicUrlBuilder;
import com.example.externalurl.util.FetchApi;
import com.example.externalurl.util.ParseJson;
import com.example.externalurl.util.ParseXml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.Map;
=======
import java.util.*;
>>>>>>> origin

@Slf4j
@Component
public class PathUrlHandler extends AbstractUrlHandler {
    public PathUrlHandler(RawApiDataLoader apiDataLoader) { super(apiDataLoader); }

    @Override
    protected Map<String, Object> executeJob(Map<String, Object> urlParam, Map<String, Object> metaInfo) {
        String apiUrl = (String) metaInfo.get("API_URL");
        String dataFormat = (String) metaInfo.get("DATA_FORMAT");
        String pageCol = (String) metaInfo.get("PAGE_COL");
        String keyName = (String) metaInfo.get("KEY_NAME");
        String totalCal = (String) metaInfo.get("TOTAL_CAL");
        String bsDtKey = (String) metaInfo.get("BSDT_KEY");
        String bsDt = (String) metaInfo.get("BSDT");
        String cntKey = (String) metaInfo.get("CNT_KEY");
        boolean isJson = dataFormat.equalsIgnoreCase("JSON");
        String reqUrlType = (String) metaInfo.get("URL_REQ_TYPE");

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> bsInfoMap = new HashMap<>();
        bsInfoMap.put("BSDT_KEY", bsDtKey);
        bsInfoMap.put("BSDT", bsDt);

<<<<<<< HEAD
        String baseUrl = DynamicUrlBuilder.buildUrl(apiUrl, urlParam, true, reqUrlType, bsInfoMap);
=======
        String baseUrl = DynamicUrlBuilder.buildUrl(apiUrl, urlParam, true, reqUrlType, bsInfoMap); // 3번째 파라미터를 기준으로 URL 생성 로직이 다름
>>>>>>> origin
        String fetchApiData = FetchApi.fetchApiData(baseUrl, dataFormat);

        if (fetchApiData.contains("RESPONSE_FAIL") || fetchApiData.isEmpty() || fetchApiData.isBlank()) {
            fetchApiData = "";
        }

        if (fetchApiData.isEmpty() || fetchApiData.isBlank()) {
            return null;
        }

        if(!fetchApiData.contains(totalCal) || !fetchApiData.contains(keyName)) {
            result.put("ERROR_CODE", "TRUE");
            result.put("HB_RESP", fetchApiData);
            result.put("IS_PAGE", true);
            return result;
        }
        result.put("HB_RESP", fetchApiData);

        int totalCnt;
        if (!cntKey.isEmpty() && !cntKey.isBlank()) {
            totalCnt = isJson ? ParseJson.responseTotalCount(fetchApiData, cntKey) : ParseXml.reponseTotalCount(fetchApiData, cntKey);
            log.info("수집 대상 데이터 개수 : {}", totalCnt);
        } else {
            totalCnt = 0;
        }

        if (totalCnt == 0) {
            log.info("수집개수 0으로 수집 종료");
            return null;
        }

        if (totalCnt > 0 || (cntKey.isEmpty() || cntKey.isBlank())) {
            if (totalCnt <= MAX_API_BATCH_SIZE) {
                result.put("IS_PAGE", false);
                List<Map<String, Object>> dataList;
                if (!(keyName.isBlank() || keyName.isEmpty())) {
                    dataList = isJson ? ParseJson.response(fetchApiData, keyName) : ParseXml.response(fetchApiData, keyName);
                } else {
                    if (isJson) {
                        dataList = ParseJson.response(fetchApiData);
                    } else {
                        throw new RuntimeException("처리할 수 없는 응답 형식 입니다.");
                    }
                
                }
                List<Map<String, Object>> dataInfo = dataList.stream().map(rowData -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("API_URL", baseUrl);
                    map.put("ROW_DATA", rowData);
                    return map;
               }).toList();
               result.put("B_RESP", dataInfo);
            }
        } else {
            // 페이지네이션 처리 시작
            result.put("IS_PAGE", true);
            int totalPageCount = (int) Math.ceil((double) totalCnt / MAX_API_BATCH_SIZE);
            log.info("총 페이지 수 : {}", totalPageCount);
            result.put("TOTAL_PAGE", totalPageCount);
            for (int page = 1; page <= totalPageCount ; page++ ){
                urlParam.replace(totalCal, 1000);
                if (page >= 2) {
                    log.info("PAGE COLUMN :{}, PAGE: {}", pageCol, page);
                    urlParam.replace(pageCol, page);
                }
                log.info("PAGE: {}, URL PARAM LIST: {}", page, urlParam);
                String pageUrl = DynamicUrlBuilder.buildUrl(apiUrl, urlParam, true, reqUrlType, bsInfoMap);
                result.put("PAGE_URL_"+page, pageUrl);
                String pageApiData = FetchApi.fetchApiData(pageUrl, dataFormat);
                List<Map<String, Object>> dataList = isJson ? ParseJson.response(pageApiData, keyName) : ParseXml.response(pageApiData, keyName);
                List<Map<String, Object>> dataInfo = dataList.stream().map(rowData -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("API_URL", pageUrl);
                    map.put("ROW_DATA", rowData);
                    return map;
                }).toList();
                result.put("B_RESP_"+page, dataInfo);
            }
        } else {
        Map<String, Object> map = new HashMap<>();
        map.put("API_URL", baseUrl);
        map.put("ROW_DATA", " ");

        result.put("IS_PAGE", false);
        result.put("B_RESP", List.of(map));
    }

<<<<<<< HEAD
    @Override
    protected Map<String, Object> executeJob(Map<String, Object> urlParam, Map<String, Object> metaInfo) {
        return null;
    }
    return result;
}
=======
}
>>>>>>> origin
