package com.example.externalurl.service;

import com.example.externalurl.repository.ExternalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExternalRequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRequestService.class);
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    ExternalRepository externalRepository;
    public List<Map<String, Object>> getUrlList(){
        List<Map<String, Object>> resultList = externalRepository.getAllUrlList();
        LOGGER.info("result List : {}", resultList.stream().toList());
        return resultList;
    }

    public List<Map<String, Object>> getCodeGroup(){
        List<Map<String, Object>> resultList = externalRepository.getCodeGroup();
        return resultList;
    }

    public Boolean getJdbcStatus(Map<String, Object> inputParameter){
        Boolean checkStatus = false;
        LOGGER.info("JDBC_URL : {}", (String)inputParameter.get("JDBC_URL"));

        try{
            Connection connection = DriverManager.getConnection((String)inputParameter.get("JDBC_URL"),
                                                            (String) inputParameter.get("JDBC_ID"),
                                                            (String) inputParameter.get("JDBC_PW"));
            checkStatus = true;
            connection.close();
        }catch (SQLException e) {
            LOGGER.info("JDBC 연결 실패");
        }
        return checkStatus;
    }

    public Boolean checkDuplicationUrl(String apiUrl){
        LOGGER.debug("checkDuplicationUrl");
        Boolean duplicationCheck = false;
        String selectResult = externalRepository.checkDuplicationUrl(apiUrl);
        if(selectResult == null || selectResult.length() == 0){
            duplicationCheck = true; // 등록가능
        }else{
            duplicationCheck = false;
        }
        return duplicationCheck;
    }

    public void saveApiUrlInfo(Map<String, Object> inputParameter){
        String ApiKey = "API"+ "-" + generateCode();
        LOGGER.info("API KEY : {} ", ApiKey);

        String JdbcKey = "JB-"+generateCode();

        // JDBC 파라미터 조립
        Map<String, Object> jdbcParam = new HashMap<>(){{
                put("ID", JdbcKey);
                put("JDBC_URL", inputParameter.get("JDBC_URL"));
                put("JDBC_DRIVER", inputParameter.get("JDBC_DRIVER"));
                put("CON_CNT", inputParameter.get("CON_CNT"));
                put("JDBC_ID", inputParameter.get("JDBC_ID"));
                put("JDBC_PW", inputParameter.get("JDBC_PW"));
        }};
        saveJdbcInfo(jdbcParam);

        // Api Info 조립
        Map<String, Object> apiInfoParam = new HashMap<>(){{
            put("API_ID", ApiKey);
            put("BASE_URL", inputParameter.get("baseUrl"));
            put("API_JB_TYPE", ((List)inputParameter.get("API_JB_TYPE")).toString());
            put("API_SVC", inputParameter.get("API_SVC"));
            put("IS_QUERY", inputParameter.get("isQueryParam"));
            put("IS_PATH", inputParameter.get("isPathParam"));
            put("DATA_FORMAT", inputParameter.get("DATA_FORMAT"));
            put("DATA_PV_GP", inputParameter.get("DATA_FORMAT"));
            put("DATA_PROVIDER", getDataProvider((String)inputParameter.get("DATA_PROVIDER")));
            put("API_EXPL", inputParameter.get("API_EXPL"));
            put("AUTH_KEY", inputParameter.get("AUTH_KEY"));
            put("AUTH_SECRET", inputParameter.get("AUTH_SECRET"));
            put("KEY_NAME", inputParameter.get("keyName"));
            put("countKeyName", inputParameter.get("countKeyName"));
            put("PAGE_COL_NM", inputParameter.get("PAGE_KEY"));
            put("TC_COL_NM", inputParameter.get("TOTAL_CNT_KEY"));
            put("JDBC_ID", JdbcKey);
        }};
        LOGGER.info("apiInfoParam : {}", apiInfoParam);
        saveApiInfo(apiInfoParam);
    }

    private String getDataProvider(String codeId){
        switch (codeId){
            case "01":
                return "국토교통부";
            case "02":
                return "통계정보서비스";
            case "03":
                return "한국부동산원";
            case "04":
                return "한국은행";
            case "05":
                return "한국주택금융공사";
        }
        return "";
    }

    public Map<String, Object> selectApiDetailInfo(Map<String, Object> apiInfoMap){
        Map<String, Object> resultMap = new HashMap<>();
        try{
            resultMap = externalRepository.selectApiDetailInfo(apiInfoMap);
            LOGGER.info("Select Result: {} ", resultMap.toString());
        }catch(Exception e){
            LOGGER.error("Select failed! {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        return resultMap;
    }

    private String generateCode(){
        String NUMBERS = "0123456789";
        String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        String firstPart = generateRandomString(NUMBERS + LETTERS, 4);
        String secondPart = generateRandomString(NUMBERS + LETTERS, 4);

        return firstPart + "-" + secondPart;
    }

    private String generateRandomString(String characterSet, int length) {
        Random random = new Random();
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characterSet.length());
            result.append(characterSet.charAt(randomIndex));
        }
        return result.toString();
    }

    private void saveJdbcInfo(Map<String, Object> jdbcMapParam){
        try{
            LOGGER.info(jdbcMapParam.toString());
            externalRepository.insertApiJdbcInfo(jdbcMapParam);
        }catch (Exception e){
            LOGGER.info(e.getMessage());
        }
    }

    private void saveApiInfo(Map<String, Object> apiMapParam){
        try{
            externalRepository.insertApiUrlInfo(apiMapParam);
        }catch (Exception e){
            LOGGER.info(e.getMessage());
        }
    }

}
