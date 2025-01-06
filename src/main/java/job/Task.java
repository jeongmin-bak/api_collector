package job;

import lombok.Getter;

@Getter
public class Task implements Runnable {
    private final Runnable task;
    private final String jobId;     // 작업 ID
    private final String srcUseType; // 수집 / 적재
    private final String srcType;   // 데이터 소스 유형
    private final int conCnt; // 커넥션 수 [논리적 개수]
    private int retryCount = 0;     // 재시도 횟수 기본값 0

    public Task(Runnable task, String jobId, String srcUseType, String srcType, int conCnt) {
        this.task = task;
        this.jobId = jobId;
        this.srcUseType = srcUseType;
        this.srcType = srcType;
        this.conCnt = conCnt;
    }

    @Override
    public void run() {
        task.run();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
