package com.example.externalurl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.externalurl.util.ShellCommandUtil;

import java.util.Map;

@Slf4j
@Service
public class RestApiStorageHandler extends AbstractStorageHandler {

    public RestApiStorageHandler(RestTemplate restTemplate, ShellCommandUtil shellCommandUtil) {
        super(restTemplate, shellCommandUtil);
    }

    @Override
    protected long executeJob(Map<String, Object> requestParam) throws Exception {
        log.info("RestApiStorageHandler - 작업 시작. 요청 파라미터: {}", requestParam);
        String command = shellCommandUtil.buildShellCommand(requestParam);
        return shellCommandUtil.runShellCommand(command);
    }
}
