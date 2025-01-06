package com.example.externalurl.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.example.externalurl.util.JsonFileUtil;
import com.example.externalurl.util.ShellCommandUtil;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public abstract class AbstractStorageHandler implements StorageHandler {

    protected RestTemplate restTemplate;

    protected ShellCommandUtil shellCommandUtil;

    @Value("${common.params.extract_tmp_dir}")
    protected String tmpDataDir;

    @Value("${agent.batch.paths.job_meta_file_path}")
    private String jsonFilePath;

    private String restUrl;

    public AbstractStorageHandler(RestTemplate restTemplate, ShellCommandUtil shellCommandUtil) {
        this.restTemplate = restTemplate;
        this.shellCommandUtil = shellCommandUtil;
    }

    @PostConstruct
    public void init() {

    }

    protected abstract long executeJob(Map<String, Object> requestParam) throws Exception;

    @Override
    public long handle(Map<String, Object> requestParam) {
        log.info("스토리지 핸들러 처리 시작");
        Map<String, Object> jobParams = new HashMap<>();
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
        String filesystem = (String) requestParam.get("SRC_TYPE");
        String srcUseType = (String) requestParam.get("SRC_USE_TYPE");
        String pgmType = (String) requestParam.get("PGM_TYPE");

        Map<String, Object> jobInfo = new HashMap<>();
        jobInfo.put("SRC_TYPE", filesystem);
        jobInfo.put("SRC_USE_TYPE", srcUseType);

        if (filesystem.equals("01")) {
            String conCnt =  String.valueOf(requestParam.get("CON_CNT"));
            jobInfo.put("CON_CNT", conCnt);
        } else {
            jobInfo.put("CON_CNT", "0");
        }

        try {
            log.info("메타 정보 파일 생성 경로: {}", jsonFilePath);
            JsonFileUtil.createJsonFile(jsonFilePath, jobParams);
            log.info("배치 작업 실행 중...");
            pid = executeJob(requestParam);
            //JobManager.registerJob(jobId, pid, pgmType, jobInfo);
            log.info("배치 작업이 성공적으로 제출되었습니다.");
        } catch (Exception e) {
            log.error("스토리지 처리 중 오류 발생: {}", e.getMessage(), e);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> payload = new HashMap<>();
            payload.put("JB_ID", requestParam.get("JB_ID").toString());
            payload.put("EXE_STSC", "99");
            payload.put("EXE_JB_RNG", requestParam.get("SRC_USE_TYPE").toString().equals("01") ? "02" : "04");
            payload.put("ERR_LOG", "배치 작업 준비에 실패했습니다: " + e.getMessage());
            HttpEntity<Map<String, Object>> responseEntity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(restUrl, responseEntity, Void.class);
        }
        return pid;
    }
}
