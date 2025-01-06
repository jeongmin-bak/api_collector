package com.example.externalurl.controller;

import com.example.externalurl.service.StorageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class ApiExecController {
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private final Map<String, StorageHandler> storageHandlerMap = new HashMap<>();


    @Value("10")
    private int concurrentJobCount;
    private String restUrl;


    @PostConstruct
    public void init(){
        log.info("초기화를 시작합니다.");
        Map<String, StorageHandler> handlers = applicationContext.getBeansOfType(StorageHandler.class);
        log.info("handlers : {}", handlers.toString());
        handlers.forEach((name, handler) -> {
            log.info("[이름: {}, 핸들러: {}]", name, handler);
            String type = handler.getClass().getSimpleName().replace("StorageHandler", "").toUpperCase();
            log.info("[유형: {}]", type);
            storageHandlerMap.put(type, handler);
            log.info("StorageHandler 초기화가 완료되었습니다.");
        });
    }

    @PostMapping("/collect/start")
    public void callApiJob(@RequestBody Map<String, Object> request){
        log.info("Agent Api Collect Start");
        try {
            log.info("api url - 수신 데이터 : {}", request);
            log.info("작업 ID: {}, 작업 날짜: {}", request.get("JB_ID"), request.get("JB_DT"));
        } catch (Exception e) {
            log.error("");
        }
    }
}
