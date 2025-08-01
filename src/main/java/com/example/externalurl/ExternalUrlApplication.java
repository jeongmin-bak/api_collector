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

        // 필수 요소 파라미터
        int contCnt = (int) metaInfo.get("CON_CNT");
        executorService = Executors.newFixedThreadPool(contCnt);

        boolean isQuery = Boolean.parseBoolean((String) metaInfo.get("IS_QUERY"));
        boolean isPath = Boolean.parseBoolean((String) metaInfo.get("IS_PATH"));
        List<Map<String, Object>> apiParam = (List<Map<String, Object>>) metaInfo.get("API_PARAM");

        log.info("apiParam 요청 갯수 : {}", apiParam.size());

        UrlHandler urlHandler;
        if (isPath) {
            urlHandler = context.getBean(PathUrlHandler.class);
        } else if(isQuery) {
            urlHandler = context.getBean(QueryUrlHandler.class);
        } else {
            throw new RuntimeException("제공되지 않는 URL 형식입니다.");
        }

        int BATCH_SIZE = apiParam.size();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for(int i = 0 ; i < BATCH_SIZE; i++) {
            final int index = i;
            if (i != 0 && i % 500 == 0) {
                try {
                    log.info("500번이상 요청되어 5초간 대기 후 요청진행합니다.");
                    Thread.sleep(5000);
                } catch(InterruptedException e) {
                    log.error(e.getMessage());
                }
            } else {
                futures.add(CompletableFuture.runAsync(() -> urlHandler.handle(apiParam.get(index), metaInfo), executorService));
            }
        }

        //List<CompletableFuture<Void>> futures = new ArrayList<>();
        //apiParam.forEach(urlParam -> {
        //    futures.add(CompletableFuture.runAsync(() -> urlHandler.handle(urlParam, metaInfo), executorService));
        //});

        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArrray(new CompletableFuture[0]));
        allDoneFuture
            .thenRun(() -> {
                log.info("모든 작업 성공");
                sendStatus.sendCompleteStatusUpdate(jbId);
            })  .exceptionally(ex -> {
                            log.error("에러 발생 : {}", ex.getMessage(), ex);
                            sendStatus.sendStatusUpdate(jbId, "99", ex.getMessage());
                            return null;
                        });
        allDoneFuture.join(); //동기적으로 기다리기
        log.info("외부데이터 수집 완료");
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("외부데이터 수집 리소스 정리 완료");
    }



}
