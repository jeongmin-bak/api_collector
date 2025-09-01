package com.example.externalurl.component;

import com.didimdp.batch.collector.agent.job.Task;
import com.didimdp.batch.collector.agent.service.NotificationService;
import com.didimdp.batch.collector.agent.util.JobManager;
import com.didimdp.batch.collector.agent.util.TaskQueueService;
import com.example.externalurl.service.NotificationService;
import com.example.externalurl.util.JobManager;
import com.example.externalurl.util.TaskQueueService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import job.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class QueueManager {
    private TaskQueueService taskQueueService;
    private final NotificationService notificationService;
    private ScheduledExecutorService scheduler;
    private final ConnectionManager connectionManager;

    @Value("${agent.execute.queue_size}")
    private int maxQueueSize;

    @Value("${agent.execute.concurrent_job_count}")
    private int concurrentJobCount;

    @Value("${agent.execute.timeout}")
    private int timeOut;

    public QueueManager(NotificationService notificationService, ConnectionManager connectionManager) {
        this.notificationService = notificationService;
        this.connectionManager = connectionManager;
    }

    @PostConstruct
    public void init() {
        this.taskQueueService = new TaskQueueService(maxQueueSize, notificationService);
        this.scheduler = Executors.newScheduledThreadPool(10);
        log.info("QueueManager 가 초기화되었습니다.");
        log.info("최대 큐 크기: {}", maxQueueSize);
    }

    public boolean addTaskToQueue(Task newTask) {
        boolean success = taskQueueService.enqueueTask(newTask, timeOut, TimeUnit.MINUTES);
        if (!success) {
            log.warn("작업 큐가 가득 찼습니다. 작업을 큐에 추가하지 못했습니다. 작업 ID: {}", newTask.getJobId());
            notificationService.notifyFailure("지정된 시간 내에 작업을 큐에 추가하지 못했습니다.", newTask.getJobId(), newTask.getSrcUseType());
        } else {
            log.info("작업이 큐에 성공적으로 추가되었습니다. 작업 ID: {}", newTask.getJobId());
        }
        return success;
    }

    public boolean removeTaskByJobId(String JobId) {
        return taskQueueService.removeTaskByJobId(JobId);
    }

    public void executePendingTasks() {
        int queueSize = taskQueueService.getQueueSize();
        int remainingCapacity = taskQueueService.getRemainingCapacity();
        log.info("동시 작업 개수 확인 - 현재 작업 수 : {}, 동시 작업 허용 수 : {}, 대기중인 작업 수 : {}, 수용 가능한 작업 수 : {}", JobManager.getTotalProcessCount(), concurrentJobCount, queueSize, remainingCapacity);
        while (JobManager.getTotalProcessCount() < concurrentJobCount) {
            Task task = taskQueueService.pollTask();
            if (task == null) {
                log.info("큐에 대기 중인 작업이 없습니다.");
                break;
            }

            if (task.getSrcType().equals("01")) {   // 디비 작업일 경우,
                boolean acquired = connectionManager.acquireConnection(task.getConCnt());
                if (!acquired) {
                    log.info("허용된 커넥션 개수 초과.");
                    this.rescheduleTask(task);
                    break;
                }
            }

            log.info("큐에서 작업을 가져와 실행합니다.");
            try {
                task.run();
                log.info("작업이 성공적으로 실행되었습니다.");

            } catch (Exception e) {
                log.error("작업 실행 중 오류가 발생했습니다: {}", e.getMessage(), e);
                notificationService.notifyFailure(e.getMessage(), task.getJobId(), task.getSrcUseType());
            }
        }
    }

    private void rescheduleTask(Task task) {

        log.info("작업을 5초후 대기 큐로 재등록합니다. 작업 ID: {}", task.getJobId());
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) scheduler;
        int activeThreads = executor.getActiveCount(); // 현재 활성화된 스레드 수
        int totalThreads = executor.getPoolSize();     // 전체 스레드 풀 크기

        if (activeThreads > totalThreads) {
            // 남은 스레드가 없는 경우 예외 처리
            log.error("스레드가 부족하여 작업을 재스케줄링할 수 없습니다. 작업 ID: {}", task.getJobId());
            throw new RuntimeException("남은 스레드가 부족하여 작업을 재스케줄링할 수 없습니다.");
        }

        task.incrementRetryCount();
        scheduler.schedule(() -> {
            log.info("작업이 대기 큐로 추가됩니다. 작업 ID: {}", task.getJobId());
            addTaskToQueue(task);
        }, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdownQueue() {
        log.info("실행 중인 모든 작업을 종료 합니다.");
        JobManager.allStopJob();
        log.info("QueueManager 가 종료됩니다. 대기중인 작업을 처리합니다.");
        scheduler.shutdown();
        taskQueueService.shutdown();
        log.info("QueueManager 가 성공적으로 종료되었습니다.");
    }
}