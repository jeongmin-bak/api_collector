package com.example.externalurl.service;

import com.example.externalurl.util.DynamicUrlBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobRunService {
    ObjectMapper objectMapper;

    public void jobRunRequest(Map<String, Object> inputParamMap){
        String JbId = (String) inputParamMap.get("JB_ID");
        String jobMeatFile = "src/unload_"+JbId+".json";

        Map<String, Object> metaInfo;
        try {
            metaInfo = objectMapper.readValue(new File(jobMeatFile), HashMap.class);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        // 필수 요소 파라미터
        String jbDt = (String) metaInfo.get("JB_DT");
        String dataProvider = (String) metaInfo.get("DATA_PROVIDER");
        String apiSvc = (String) metaInfo.get("API_SVC");                   // 서비스 이름, 테이블 이름
        String baseUrl = (String) metaInfo.get("baseUrl");
        String keyName = (String) metaInfo.get("keyName");
        String countKeyName = (String) metaInfo.getOrDefault("countKeyName", "none");
        String returnType = (String) metaInfo.get("returnType");
        List<String> pathParams = (List<String>) metaInfo.get("pathParams"); // Path Parameters
        Map<String, String> queryParams = (Map<String, String>) metaInfo.get("queryParams"); // Query Parameters

        // URL 생성
        String apiUrl = DynamicUrlBuilder.buildUrl(baseUrl, pathParams, queryParams);
//        String fetchApiData = FetchApi.fetchApiData(apiUrl);
//        List<Map<String, Object>> response;
//        if (returnType.equalsIgnoreCase("json")) {
//            response = ParseJson.response(fetchApiData, keyName);
//        } else {
//            response = ParseXml.response(fetchApiData, keyName);
//        }
        //log.info("추출 후 데이터 : {}", response);
    }


}
