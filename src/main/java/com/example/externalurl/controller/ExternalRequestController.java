package com.example.externalurl.controller;

import com.example.externalurl.service.ExternalRequestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ExternalRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRequestController.class);

    private final ExternalRequestService externalRequestService;

    @PostMapping("/getExternalUrlList")
    public Map<String, Object> getUrlList(){
        LOGGER.info("Request Success!!");
        List<Map<String, Object>> urlList =  externalRequestService.getUrlList();
        LOGGER.info("Url List : {}", urlList.stream().toList());
        Map<String, Object> resultList = new HashMap<>();
        resultList.put("resultList", urlList);
        return resultList;
    }

    @PostMapping("/getJdbcConnStatus")
    public Map<String, Object> checkJdbcConnStatus(@RequestBody Map<String, Object> inputParameter){
        LOGGER.debug("Jdbc Info inputParameter : ", inputParameter.get("JDBC_URL").toString());
        Boolean resultStatus = externalRequestService.getJdbcStatus(inputParameter);
        LOGGER.debug("Response Result : ", resultStatus);
        return Map.of("connectStatus", resultStatus);
    }

    @PostMapping("/checkDuplication")
    public Map<String, Object> checkDuplicationUrl(@RequestBody Map<String, Object> inputParameter){
        LOGGER.debug("apiUrl inputParameter : ", inputParameter.get("API_URL").toString());
        Boolean result = externalRequestService.checkDuplicationUrl((String) inputParameter.get("API_URL"));
        LOGGER.debug("Response Result : ", result);
        return Map.of("duplicationCheck", result);
    }

    @PostMapping("/saveApiUrlInfo")
    public void saveApiUrlInfo(@RequestBody Map<String, Object> inputParameter){
        LOGGER.info("apiURL save");
        LOGGER.info("apiUrl Save Start : {}", inputParameter.toString());
        externalRequestService.saveApiUrlInfo(inputParameter);
    }
}
