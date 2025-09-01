package com.example.externalurl.component;
import com.example.externalurl.util.JobManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler {

    private final ConnectionManager connectionManager;
    private final QueueManager queueManager;

    @Async
    @Scheduled(fixedRateString = "${agent.execute.job_check_interval}")
    public void runningJobsInfo() {
        JobManager.runningJobsInfo();
    }

    @Async
    @Scheduled(fixedRateString = "${agent.execute.cleanup_interval:3000}")
    public void cleanupJobs() {
        Map<String, Map<String, Object>> stringMapMap = JobManager.cleanupJobs();
        if (!stringMapMap.isEmpty()) {
            stringMapMap.forEach((jobInfo, value) -> {
                log.info("비정상 종료된 작업[{}] 리소스 정리", jobInfo);
                if (value.get("SRC_TYPE").equals("01")) {
                    connectionManager.releaseConnection(Integer.parseInt((String) value.get("CON_CNT")));
                }
                try {
                    log.info("비정상 종료된 작업의 메타 파일 삭제 : {}", value.get("JSON_FILE_NAME"));
                    Files.deleteIfExists(Path.of((String) value.get("JSON_FILE_NAME")));
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }

                log.info("비정상 종료된 작업의 데이터 파일 삭제 : {}", value.get("TMP_DIR"));
                Path rootPath = Paths.get((String) value.get("TMP_DIR"));
                if (!Files.exists(rootPath)) {
                    log.warn("지정된 경로가 존재하지 않습니다: {}", rootPath);
                    return;
                }

                try {
                    // 하위 파일 및 디렉토리를 탐색하여 삭제
                    try (Stream<Path> paths = Files.walk(rootPath)) {
                        paths.sorted((p1, p2) -> p2.getNameCount() - p1.getNameCount()) // 하위 항목부터 삭제
                                .forEach(path -> {
                                    try {
                                        Files.delete(path);
                                        log.info("삭제 성공: {}", path);
                                    } catch (IOException e) {
                                        log.error("삭제 실패: {}. 오류: {}", path, e.getMessage());
                                    }
                                });
                    }
                    log.info("디렉토리 및 모든 하위 항목 삭제 완료: {}", rootPath);
                } catch (IOException e) {
                    log.error("디렉토리 삭제 중 오류 발생: {}", e.getMessage());
                }
            });
        }
    }

    @Async
    @Scheduled(fixedRateString = "${agent.execute.poll_interval}")
    public void executePendingTasks() {
        queueManager.executePendingTasks();
    }
}
