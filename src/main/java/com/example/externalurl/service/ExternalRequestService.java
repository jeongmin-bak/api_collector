package com.example.externalurl.service;

import com.example.externalurl.repository.ExternalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ExternalRequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRequestService.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ExternalRepository externalRepository;

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
        String JbId = "JOB"+ getJobDate() + generateCode();
        String JbDt = getJobDate();
        LOGGER.info("JbId : {} ", JbId);
        LOGGER.info("JbDt : {}", JbDt);

        inputParameter.put("JB_ID", JbId);
        inputParameter.put("JB_DT", JbDt);

        try{
            objectMapper.writeValue(new File("src/unload_"+JbId+".json"), inputParameter);
        }catch (Exception e){
            LOGGER.info("Error : {} ", e.getMessage());
            LOGGER.info("작업을 저장에 실패하였습니다");
            throw new RuntimeException(e);
        }
    }
    private String generateCode(){
        String NUMBERS = "0123456789";
        String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        String randomStr = generateRandomString(NUMBERS + LETTERS, 9);

        return randomStr;
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

    private String getJobDate(){
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String jobDate = currentDate.format(formatter);

        return jobDate;
    }
}
