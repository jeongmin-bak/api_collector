package com.example.externalurl.service;

import com.example.externalurl.repository.MySqlMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CommonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonService.class);

    public final MySqlMapper mySqlMapper;

    public Map<String, String> getCodeGroupId() throws Exception{
        HashMap<String, Object> testMap = new HashMap<>(){{
            put("DATA_PROVICER", "국토교통부");
        }};
        String codeGroupId = mySqlMapper.getCodeGroupId(testMap);
        LOGGER.info("Select Result : {}", codeGroupId);
        return Map.of("GROUP_ID", codeGroupId);
    }
}
