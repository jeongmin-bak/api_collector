package com.example.externalurl.service;

import com.example.externalurl.controller.ExternalRequestController;
import com.example.externalurl.repository.ExternalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ExternalRequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRequestService.class);
    @Autowired
    ExternalRepository externalRepository;
    public List<Map<String, Object>> getUrlList(){
        List<Map<String, Object>> resultList = externalRepository.getAllUrlList();
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
            duplicationCheck = false;
        }else{
            duplicationCheck = true;
        }
        return duplicationCheck;
    }

    public void saveApiUrlInfo(Map<String, Object> inputParameter){
        String ApiKey = "API"+ "-" + generateCode();
        LOGGER.info("API KEY : {} ", ApiKey);

        // JDBC 파라미터 조립
        Map<String, Object> jdbcParam = new HashMap<>(){{
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
            put("baseUrl", inputParameter.get("baseUrl"));
            put("AUTH_KEY", inputParameter.get("AUTH_KEY"));
            put("AUTH_SECRET", inputParameter.get("AUTH_SECRET"));
            put("IS_QUERY", inputParameter.get("isQueryParam"));
            put("IS_PATH", inputParameter.get("isPathParam"));
            put("DATA_FORMAT", inputParameter.get("returnType"));
            put("keyName", inputParameter.get("keyName"));
            put("DATA_PROVIDER", inputParameter.get("DATA_PROVIDER"));
        }};
        saveApiInfo(apiInfoParam);
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
            LOGGER.info(apiMapParam.toString());
            externalRepository.insertApiUrlInfo(apiMapParam);
        }catch (Exception e){
            LOGGER.info(e.getMessage());
        }
    }
}
