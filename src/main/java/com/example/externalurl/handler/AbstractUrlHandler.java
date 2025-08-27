package com.example.externalurl.handler;

import com.example.externalurl.component.RawApiDataLoader;
import com.example.externalurl.util.DynamicUrlBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractUrlHandler implements UrlHandler {
    private final RawApiDataLoader apiDataLoader;
    protected final int MAX_API_BATCH_SIZE = 1000;
    protected abstract Map<String, Object> executeJob(Map<String, Object> urlParam, Map<String, Object> metaInfo);

    @Override
    @Transactional
    public void handle(Map<String, Object> urlParam, Map<String, Object> metaInfo) {
        String dataFormat = (String) metaInfo.get("DATA_FORMAT");
        String jbDt = (String) metaInfo.get("JB_DT");
        String dataPvGp = (String) metaInfo.get("DATA_PV_GP");
        String apiUrl = (String) metaInfo.get("API_URL");
        String apiSvc = (String) metaInfo.get("API_SVC");
        String dataProvider = (String) metaInfo.get("DATA_PROVIDER");
        String apiExpl = (String) metaInfo.get("API_EXPL");
        String bsDtKey = (String) metaInfo.get("BSDT_KEY");
        String bsDt = metaInfo.get("BSDT") == null ? "" : (String) metaInfo.get("BSDT");
        String bsDtParamOrder = metaInfo.get("BSDT_ORDER") == null ? null : metaInfo.get("BSDT_ORDER").toString();

        Map<String, Object> bsInfoMap = new HashMap<>();
        bsInfoMap.put("BSDT_KEY", bsDtKey);
        bsInfoMap.put("BSDT", bsDt);
        bsInfoMap.put("BSDT_ORDER", bsDtParamOrder);
        log.info("bsInfoMap : {}", bsInfoMap);

        boolean isPath = Boolean.parseBoolean((String) metaInfo.get("IS_PATH"));
        String reqUrlType = metaInfo.get("URL_REQ_TYPE").toString();
        String baseUrl = DynamicUrlBuilder.builderUrl(apiUrl, urlParam, isPath, reqUrlType, bsInfoMap);
        log.info("baseUrl : {}", baseUrl);

        Map<String, Object> apiData = executeJob(urlParam, metaInfo);

        if(apiData == null || apiData.isEmpty()) {
            log.info("fetchApiData is null");
            return;
        }
        boolean isPage = (boolean) apiData.get("IS_PAGE");
        String jbDt_formatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String seq = UUID.randomUUID().toString();
        String dataSeqId = apiSvc + "_" + seq;

        if(apiData.get("ERROR_CODE") != null) {
            if(apiData.get("ERROR_CODE").toString().equals("TRUE")) {
                String errorContent = apiData.get("HB_RESP").toString();
                log.info("ERROR_CODE, content : {} {}", apiData.get("ERROR_CODE"), errorContent);
                apiDataLoader.deleteRawApiData(baseUrl, bsDt);
                apiDataLoader.insertRawApiData(dataPvGp, apiSvc, baseUrl, dataSeqId, dataProvider, apiExpl, errorContent, dataFormat, bsDt, jbDt_formatted);
                return;
            }
        }
        String hbResp = (String) apiData.get("HB_RESP");

        // 서비스키가 다르면 중복되는 데이터가 저장되기 때문에 서비스키 제거하는 작업 필요
        ObjectMapper mapper = new ObjectMapper();
        if(!isPage) {
            String removeTokenUrl = DynamicUrlBuilder.removeServiceKey(baseUrl);
            apiDataLoader.deleteRawApiDataDetail(removeTokenUrl, bsDt);

            List<Map<String, Object>> bResp = (List<Map<String, Object>>) apiData.get("B_RESP");
            bResp.forEach(map -> {
                String seqValue = UUID.randomUUID().toString();
                String apiSvcId = apiSvc + "_" + seqValue;
                String json;
                try {
                    json = mapper.writeValueAsString(map.get("ROW_DATA"));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                apiDataLoader.insertRawApiDataDetail(dataPvGp, apiSvc, removeTokenUrl, apiSvcId, dataProvider, apiExpl, json, dataFormat, bsDt, jbDt_formatted);
            });
        } else {
            int totalPage = (int) apiData.get("TOTAL_PAGE");
            for(int page = 1; page <= totalPage; page++) {
                String pageUrl = (String) apiData.get("PAGE_URL_") + page;
                String removeTokenPageUrl = DynamicUrlBuilder.removeServiceKey(pageUrl);

                apiDataLoader.deleteRawApiData(removeTokenPageUrl, bsDt);
                apiDataLoader.insertRawApiData(dataPvGp, apiSvc, removeTokenPageUrl, dataSeqId, dataProvider, apiExpl, hbResp, dataFormat, bsDt, jbDt_formatted);

                apiDataLoader.deleteRawApiDataDetail(removeTokenPageUrl, bsDt);

                for (int page = 1; page <= totalPage; page++) {
                    List<Map<String, Object>> bResp = (List<Map<String, Object>>) apiData.get("B_RESP_" + page);
                    bResp.forEach(map -> {
                        String json;
                        try {
                            json = mapper.writeValueAsString(map.get("ROW_DATA"));
                        } catch (JsonProcessingException e ) {
                            throw new RuntimeException(e);
                        }
                        String seqValue = UUID.randomUUID().toString();
                        String apiSvcId = apiSvc + "_" + seqValue;
                        String removeTokenPageUrl = DynamicUrlBuilder.removeServiceKey((String) map.get("API_URL"));
                        apiDataLoader.insertRawApiDataDetail(dataPvGp, apiSvc, removeTokenPageUrl, apiSvcId, dataProvider, apiExpl, json, dataFormat, bsdt, jbDt_formatted);
                    });
                }
            }
        }
        log.info("{} : 수집작업완료", baseUrl);
    }
}