package com.example.externalurl.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import util.JsonFileUtil;
import util.ShellCommandUtil;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public abstract class AbstractStorageHandler implements StorageHandler{
    protected RestTemplate restTemplate;
    protected ShellCommandUtil shellCommandUtil;

    @Value("${common.params.extract_tmp_dir}")
    protected String tmpDataDir;

    @Value("${agent.batch.paths.job_meta_file_path}")
    private String jsonFilePath;

    private String restUrl;

    @PostConstruct
    public void init() {

    }

    protected abstract long executeJob(Map<String, Object> requestParam) throws Exception;

    @Override
    public long handle(Map<String, Object> requestParam) {
        log.info("Storage Handler Process start!");
        Map<String, Object> jobParams = new HashMap<>();
        log.info("jobParam : {}", jobParams.toString());
        requestParam.forEach((key, value) -> {
            if (value instanceof Map<?, ?>) {
                ((Map<?, ?>) value).forEach((innerKey, innerValue) -> {
                    jobParams.put(innerKey.toString(), innerValue);
                });
            } else {
                jobParams.put(key, value);
            }
        });

        long pid = 0;
        String jobId = (String) requestParam.get("JB_ID");
        Map<String, Object> jobInfo = new HashMap<>();

        try {
            log.info("메타 정보 파일 생성 경로 : {}", jsonFilePath);
            JsonFileUtil.createJsonFile(jsonFilePath, jobParams);
            log.info("작업 실행 중 ...");
            pid = executeJob(requestParam);
        } catch (Exception e){

        }
        return pid;

    }


}
