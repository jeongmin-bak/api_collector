package com.example.externalurl.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ParseJson {
    static ObjectMapper objectMapper = new ObjectMapper();

    public static int responseTotalCount(String jsonResponse, String keyName) {
        JsonNode rootNode;
        try  {
            rootNode = objectMapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (rootNode.has("SttsApiTblData") || rootNode.has("SttsApiTbl") || rootNode.has("StanReginCd")) {
            JsonNode totalCountKey = getListTotalCountKey(rootNode, keyName);
            return totalCountKey.asInt();
        } else {
            JsonNode totalCountNode = searchNestedKey(rootNode, keyName);
            if (totalCountNode != null) {
                return totalCountNode.asInt();
            } else {
                log.error("Key '{}' not found in the JSON response", keyName);
                throw new RuntimeException(String.format("key '{%s}' not found in the JSON response", keyName));
            }
        }
    }

    public static List<Map<String, Object>> response(String jsonResponse) {
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> response(String jsonResponse, String keyName) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        // 한 응답값에 리스트 -> 맵 안에 리스트, 맵이 들어있는 형태처리
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object tmpObject = mapper.readValue(jsonResponse, Object.class);
            if(tmpObject instanceof List) {
                List<Map<String, Object>> tmpList = mapper.readValue(jsonResponse, new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> itemMap : tmpList) {
                    log.info("itemMap {}", itemMap);
                    if(itemMap.containsKey(keyName)) {
                        Object childNode = itemMap.get(keyName);
                        if(childNode instanceof Map) {
                            dataList = new ArrayList<>() {{
                                add((Map<String, Object>) childNode);
                            }};
                        } else {
                            dataList = (List)childNode;
                        }
                    }
                }
            } else {
                // 가장 바깥쪽 데이터가 리스트가 아닌경우
                try {
                    // JSON 응답을 루트 노드로 변환
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    if(rootNode.has("SttsApiTblData") || rootNode.has("SttsApiTbl") || rootNode.has("StanReginCd")) {
                        JsonNode dataArray;
                        if(rootNode.has("SttsApiTblData")) {
                            dataArray = rootNode.get("SttsApiTblData");
                        } else if (rootNode.has("SttsApiTbl")) {
                            dataArray = rootNode.get("SttsApiTbl");
                        } else {
                            dataArray = rootNode.get("StanReginCd");
                        }

                        if (dataArray != null && dataArray.isArray()) {
                            for (JsonNode dataGroup : dataArray) {
                                JsonNode targetArray = dataGroup.get(keyName);
                                if (targetArray != null && targetArray.isArray()) {
                                    for (JsonNode fieldNode : targetArray) {
                                        Map<String, Object> fieldMap = objectMapper.convertValue(fieldNode, Map.class);
                                        dataList.add(fieldMap);
                                    }
                                }
                            }
                        }
                    } else {
                        JsonNode targetNode = searchNestedKey(rootNode, keyName);
                        if (targetNode != null) {
                            if (targetNode.isArray()) {
                                for (JsonNode fieldNode : targetNode) {
                                    Map<String, Object> fieldMap = objectMapper.convertValue(fieldNode, Map.class);
                                    dataList.add(fieldMap);
                                }
                            } else if (targetNode.isObject()) {
                                Map<String, Object> fieldMap = objectMapper.convertValue(targetNode, Map.class);
                                dataList.add(fieldMap);
                            } else {
                                log.error("key '{}' found but is neither array nor object", keyName);
                            }
                        } else {
                            log.error("Key '{}' not found", keyName);
                        }
                    }
                } catch (IOException e) {
                    log.error("JSON 응답 파싱 중 오류 발생: {}", e.getMessage());
                    throw new RuntimeException("JSON 응답 파싱 실패", e);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return dataList;
    }

    private static JsonNode searchNestedKey(JsonNode rootNode, String keyName) {
        if (rootNode.has(keyName)) {
            JsonNode targetNode = rootNode.get(keyName);
            if (targetNode.isObject() && targetNode.has(keyName)) {
                return targetNode.get(keyName);
            }
            return targetNode;
        }
        for (Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            JsonNode childNode = entry.getValue();
            if (childNode.isObject()) {
                JsonNode result = searchNestedKey(childNode, keyName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static JsonNode getListTotalCountKey(JsonNode rootNode, String keyName) {
        if (rootNode.has("SttsApiTblData") || rootNode.has("SttsApiTbl") || rootNode.has("StanReginCd")) {
            JsonNode apiTbl;
            if (rootNode.has("SttsApiTblData")) {
                apiTbl = rootNode.get("SttsApiTblData");
            } else if (rootNode.has("SttsApiTbl")) {
                apiTbl = rootNode.get("SttsApiTbl");
            } else {
                apiTbl = rootNode.get("StanReginCd");
            }

            if (apiTbl != null && apiTbl.isArray()) {
                JsonNode head = apiTbl.get(0).path("head");
                if (head.isArray()) {
                    for (JsonNode meta : head) {
                        if (meta.has(keyName)) {
                            return meta.get(keyName);
                        }
                    }
                }
            }
        }
        throw new RuntimeException(String.format("Key '{%s}' not found in the JSON response", keyName));
    }
}