package com.example.externalurl.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTest.class);

    @Autowired
    ExternalRequestService externalRequestService;
    @Autowired
    CommonService commonService;

    @Test
    public void test() throws Exception {
        LOGGER.info("Oracle Result : ", externalRequestService.getUrlList().get(0));
        LOGGER.info("MySql Result : ", commonService.getCodeGroupId());
    }
}
