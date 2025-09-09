package com.example.externalurl.util;
import com.example.externalurl.service.NotificationService;
import job.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TaskQueueService {
    private final BlockingQueue<Task> taskQueue;
    private final NotificationService notificationService;

    public TaskQueueService(int maxQueueSize, NotificationService notificationService) {
        taskQueue = new ArrayBlockingQueue<>(maxQueueSize);
        this.notificationService = notificationService;
        log.info("TaskQueueService 가 초기화되었습니다. 큐 최대 크기: {}", maxQueueSize);
    }

    public boolean enqueueTask(Task newTask, long timeout, TimeUnit unit) {
        try {
            return taskQueue.offer(newTask, timeout, unit);
        } catch (InterruptedException e) {
            log.error("작업 큐 추가가 인터럽트되었습니다. 작업 ID: {}, 오류: {}", newTask.getJobId(), e.getMessage(), e);
            notificationService.notifyFailure("작업 제출이 중단되었습니다.", newTask.getJobId(), newTask.getSrcUseType());
            return false;
        }
    }

    public Task pollTask() {
        return taskQueue.poll();
    }

    public int getQueueSize() {
        return this.taskQueue.size();
    }

    public int getRemainingCapacity() {
        return taskQueue.remainingCapacity();
    }

    public boolean removeTaskByJobId(String jobId) {
        return taskQueue.removeIf(task -> task.getJobId().equals(jobId));
    }

    public void shutdown() {
        log.info("TaskQueueService 종료 중입니다. 대기 중인 작업을 확인합니다.");
        notifyPendingTasksOnShutdown();
    }

    private void notifyPendingTasksOnShutdown() {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            if (task != null) {
                log.warn("작업이 시스템 종료로 인해 종료됩니다. 작업 ID: {}", task.getJobId());
                notificationService.notifyFailure(
                        "시스템 종료로 인해 작업이 중단되었습니다.",
                        task.getJobId(),
                        task.getSrcUseType()
                );
            }
        }
        log.info("TaskQueueService 가 정상적으로 종료되었습니다.");
    }
}