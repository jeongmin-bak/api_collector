package com.example.externalurl.controller;

import com.example.externalurl.repository.ExternalRepository;
import com.example.externalurl.service.DbStorageHandler;
import com.example.externalurl.service.ExternalRequestService;
import com.example.externalurl.service.StorageHandler;
import com.example.externalurl.util.ShellCommandUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import job.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/batch/external")
@RequiredArgsConstructor
public class ApiExecController {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final DiscoveryReConnectUtil discoveryReConnectUtil;
    private final ShellCommandUtil shellCommandUtil;
    private final QueueManager queueManager;

    @Value("${common.params.extract_tmp_dir}")
    protected String tmpDataDir;

    @Value("${agent.batch.paths.job_meta_file_path}")
    private String jsonFilePath;

    @Value("${agent.batch.shell_scripts.start_api_batch}")
    private String javaShellScript;

    @Value("${server.agent.url}")
    private String agentUrl;

    @Value("${server.agent.port}")
    private String agentPort;

    @PostConstruct
    public void init(){
        log.info("BatchExternalController 초기화가 완료되었습니다.");
    }

    @PostMapping("/execute")
    public void executeBatchJob(@RequestBody Map<String, Object> request){
        String jobId = null;
        try {
            String rstJson = (String) request.get("RST");
            log.info("외부데이터 수집 작업 요청 수신 - 요청 데이터: {}", request);

            Map<String, Object> requestParam = objectMapper.readValue(rstJson, new TypeReference<> {
            });

            synchronized (this) {
                jobId = requestParam.get("JB_ID").toString();
                jbDt = requestParam.get("JB_DT").toString();
                log.info("동시 작업 허용 개수 내에서 작업을 처리합니다. 작업 ID: {}", jobId);
                log.info("외부데이터 작업 요청처리 시작");

                Map<String, Object> jobParams = new HashMap<>();
                requestParam.forEach((key, value) -> {
                    if (value instanceof Map<?, ?>) {
                        ((Map<?, ?> value).forEach((innerKey, innerValue) -> {
                            jobParams.put(innerKey.toString(), innerValue);
                        })
                    } else {
                        jobParams.put(key, value);
                    }
                });
                log.info("외부데이터 작업 메타 정보 : {}", jobParams);
            }

        } catch (Exception e) {
            String errorMessage = "요청 처리 중 오류가 발생했습니다. 상세 정보: " + e.getMessage() + ". 관리자에게 문의하세요.";
            log.error("요청 처리 중 예외 발생 : {}", e.getMessage());
            this.sendErrorStatus(jobId, "01", errorMessage);
        }

    }



    
    @PostMapping("/execute/api/collector")
    public void executeApiJob(@RequestBody Map<String, Object> request){
        log.info("Agent Api Collect Start");
        request = AddJobInfo(request);
        try {
            log.info("api url - 수신 데이터 : {}", request);
            log.info("작업 ID: {}, 작업 날짜: {}", request.get("JB_ID"), request.get("JB_DT"));

            LinkedHashMap<String, Object> makeParamMap = makeJsonParam(request);
            String saveResult = createJsonApiFile(makeParamMap);

            if(saveResult.equals("success")){
                // 커멘드 작업을 시작합니다.
                String command = buildShellCommand(request);
                log.info("실행 쉘 명령어 : {}", command);
                //long pid = shellCommandUtil.runShellCommand(command);
                //log.info("작업 PID : {}", pid);
            }
        } catch (Exception e) {
            log.error("Api 외부 데이터 수집 실패 : {}", e.getMessage());
        }
    }

    private String createJsonApiFile(LinkedHashMap<String, Object> params){
        String saveStatus = "";
        try{
            String filePath = jsonFilePath + "/api_collector_" + params.get("JB_ID") + ".json";
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(filePath), params);
            saveStatus = "success";

            log.info("작업을 위한 JSON 파일이 성공적으로 생성되었습니다. 파일 경로: {}", filePath);
        }catch (Exception e){
            log.error("JSON File Save failed !! 작업 ID : {} ", params.get("JB_ID"));
        }
        return saveStatus;
    }

    // JSON 파일로 만들기 위한 구성 메소드
    private LinkedHashMap<String, Object> makeJsonParam(Map<String, Object> userParamMap){
        log.info("파라미터 구성을 시작합니다.");
        LinkedHashMap<String, Object> tmpMap = new LinkedHashMap<>();

        // 공통 parameter
        tmpMap.put("JB_ID", userParamMap.get("JB_ID"));
        tmpMap.put("JB_DT", userParamMap.get("JB_DT"));
        tmpMap.put("API_SVC", userParamMap.get("API_SVC"));
        tmpMap.put("baseUrl", userParamMap.get("BASE_URL"));
        tmpMap.put("pathParams", userParamMap.get("pathParams"));
        tmpMap.put("queryParams", userParamMap.get("queryParams"));
        tmpMap.put("isPathParam", userParamMap.get("IS_PATH"));
        tmpMap.put("isQueryParam", userParamMap.get("IS_QUERY"));
        tmpMap.put("keyName", userParamMap.get("KEY_NAME"));
        tmpMap.put("countKeyName", userParamMap.get("COUNTKEYNAME"));
        tmpMap.put("returnType", userParamMap.get("DATA_FORMAT"));
        tmpMap.put("DATA_PROVIDER", userParamMap.get("DATA_PROVIDER"));
        tmpMap.put("JDBC_ID", userParamMap.get("JDBC_ID"));
        tmpMap.put("JDBC_PW", userParamMap.get("JDBC_PW"));
        tmpMap.put("JDBC_URL", userParamMap.get("JDBC_URL"));
        tmpMap.put("JDBC_DRIVER", userParamMap.get("JDBC_DRIVER"));
        tmpMap.put("CON_CNT", userParamMap.get("CONN_POOL").toString());

        return tmpMap;
    }

    private String buildShellCommand(Map<String, Object> params){
        StringBuilder command = new StringBuilder();
        command.append("sh").append(" ");
        String apiScriptPath = "/Users/parkjungmin/Desktop/external_url/api_dir/apiurl_test_script.sh";
        // 스크립트 경로 임시 지정
        command.append(apiScriptPath).append(" ");

        if (params.containsKey("JB_ID")) {
            //command.append("api_collector_");
            command.append(params.get("JB_ID")).append(" ");
        }
        if (params.containsKey("JB_DT")) {
            command.append(params.get("JB_DT")).append(" ");
        }

        log.info("생성된 쉘 명령어: {}", command.toString().trim());
        return command.toString().trim();
    }

    private Map<String, Object> AddJobInfo(Map<String, Object> userParamMap){
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        //JB_DT 생성
        String jbDt = now.format(formatter);

        // API -> JOB_ID DB insert
        if(userParamMap.get("JOB_ID") == null){
            //JB_ID 생성
            String jbId = "JOB"+ jbDt + randomJobId();

            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("API_ID", userParamMap.get("API_ID"));
            updateParam.put("JOB_ID", jbId);
            externalRepository.updateJobId(updateParam);
            log.info("작업 ID update 완료");

            userParamMap.put("JB_DT", jbDt);
            userParamMap.put("JB_ID", jbId);
        } else {
            log.info("기존 작업 ID가 존재합니다.");
        }

        return userParamMap;
    }

    private String randomJobId(){
        String str = "0123456789abcdefghijklmnopqrstuvwxyz";

        return new Random().ints(8, 0, str.length())
                .mapToObj(i -> String.valueOf(str.charAt(i)))
                .collect(Collectors.joining());
    }
}
