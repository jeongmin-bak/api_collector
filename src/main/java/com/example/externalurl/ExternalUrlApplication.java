package com.example.externalurl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ExternalUrlApplication implements CommandLineRunner{

    private final ObjectMaper objectMapper;
    private final ApplicationContext context;
    private final SendStatus sendStatus;

    @Value("${common.params.meta_info_path}")
    private String jsonFilePath;

    private ExecutorService executorService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ExternalUrlApplication.class)
                            .web(WebApplicationType.NONE)
                            .run(args)
        SpringApplication.exit(context, () -> 0);
        SpringApplication.run(ExternalUrlApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String jbId = args[0];
        log.info("외부 데이터 수집 시작");
        sendStatus.sendStatusUpdate(jbId, "01", ""); // 외부데이터 작업 시작 전송 신호
        String jobMetaFile = jsonFilePath + "/api_unload_" + jbId + ".json";
        log.info("메타 파일 이름 : {}", jobMetaFile);

        Map<String, Object> metaInfo;
        try {
            metaInfo = objectMapper.readValue(new File(jobMetaFile), HashMap.class);
        } catch (IOException e) {
            log.error("작업 메타 정보를 읽어 오는데 실패 했습니다 : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
