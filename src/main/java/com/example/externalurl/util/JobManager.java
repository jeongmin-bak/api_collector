package com.example.externalurl.util;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JobManager {
    private static final ConcurrentHashMap<String, Map<String, Object>> jobJavaMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Map<String, Object>> jobProcMap = new ConcurrentHashMap<>();

    private JobManager() {
    }

    public static int getTotalProcessCount() {
        long javaProcessCount = jobJavaMap.values().stream()
                .map(map -> (ProcessHandle) map.get("PROCESS"))
                .filter(ProcessHandle::isAlive)
                .count();

        long procProcessCount = jobProcMap.values().stream()
                .map(map -> (ProcessHandle) map.get("PROCESS"))
                .filter(ProcessHandle::isAlive)
                .count();

        return (int) (javaProcessCount + procProcessCount);
    }

    public static synchronized void registerJob(String jobId, long pid, String pgmType, Map<String, Object> jobInfo) {
        if (jobJavaMap.containsKey(jobId) || jobProcMap.containsKey(jobId)) {
            log.error("중복된 작업 ID: {}", jobId);
            throw new RuntimeException("중복된 작업 ID: " + jobId);
        }
        ProcessHandle processHandle = ProcessHandle.of(pid).orElseThrow(() -> new RuntimeException("유효하지 않은 PID: " + pid));

        if (!processHandle.isAlive()) {
            log.error("PID {}를 가진 프로세스가 실행 중이 아닙니다.", pid);
            throw new RuntimeException("PID " + pid + "를 가진 프로세스가 실행 중이 아닙니다.");
        }

        jobInfo.put("PROCESS", processHandle);

        if (pgmType.equals("01")) {
            jobJavaMap.put(jobId, jobInfo);
            log.info("Java 작업 등록: 작업 ID: {}, PID: {}", jobId, pid);
        } else {
            jobProcMap.put(jobId, jobInfo);
            log.info("Proc 작업 등록: 작업 ID: {}, PID: {}", jobId, pid);
        }
    }

    public static synchronized void stopJob(String jobId) {
        if (!jobJavaMap.isEmpty()) {
            Optional<Map<String, Object>> optional = Optional.ofNullable(jobJavaMap.get(jobId));
            if (optional.isPresent()) {
                Map<String, Object> jobInfo = optional.get();
                ((ProcessHandle) jobInfo.get("PROCESS")).destroy();
                log.info("Java 작업 중지 시작: 작업 ID: {}, PID: {}", jobId, ((ProcessHandle) jobInfo.get("PROCESS")).pid());
                return;
            }
        }

        if (!jobProcMap.isEmpty()) {
            Optional<Map<String, Object>> optional = Optional.ofNullable(jobProcMap.get(jobId));
            if (optional.isPresent()) {
                Map<String, Object> jobInfo = optional.get();
                ((ProcessHandle) jobInfo.get("PROCESS")).destroy();
                log.info("Proc 작업 중지 시작: 작업 ID: {}, PID: {}", jobId, ((ProcessHandle) jobInfo.get("PROCESS")).pid());
                return;
            }
        }

        log.error("실행 중인 작업을 찾을 수 없습니다. 작업 ID: {}", jobId);
        throw new NoSuchElementException("실행 중인 작업을 찾을 수 없습니다. 작업 ID: " + jobId);
    }

    public static void allStopJob() {
        if (jobJavaMap.isEmpty() && jobProcMap.isEmpty()) {
            log.info("정리할 작업이 없습니다.");
            return;
        }

        jobJavaMap.forEach((jobId, value) -> {
            ProcessHandle processHandle = (ProcessHandle) value.get("PROCESS");
            if (processHandle.isAlive()) {
                log.info("{} 작업을 종료합니다.", jobId);
                processHandle.destroy();
            }
        });

        jobProcMap.forEach((jobId, value) -> {
            ProcessHandle processHandle = (ProcessHandle) value.get("PROCESS");
            if (processHandle.isAlive()) {
                log.info("{} 작업을 종료합니다.", jobId);
                processHandle.destroy();
            }
        });
    }

    public static synchronized Map<String, Object> removeJob(String jobId) {
        if (jobJavaMap.isEmpty() && jobProcMap.isEmpty()) {
            log.info("제출된 작업이 없습니다.");
            throw new NoSuchElementException("제출된 작업이 없습니다");
        }

        log.info("작업 제거 요청 - 작업 ID: {}", jobId);
        Map<String, Object> removeJavaJob = jobJavaMap.remove(jobId);
        Map<String, Object> removeProcJob = jobProcMap.remove(jobId);

        ProcessHandle processHandle;
        if (removeJavaJob != null) {
            processHandle = (ProcessHandle) removeJavaJob.get("PROCESS");
        } else {
            processHandle = (ProcessHandle) removeProcJob.get("PROCESS");
        }

        Optional<ProcessHandle> jobHandle = Optional.ofNullable(processHandle);
        ProcessHandle removeProcessJob = jobHandle.orElseThrow(() -> {
            log.error("작업을 찾을 수 없습니다. 작업 ID: {}", jobId);
            return new NoSuchElementException("작업 ID를 찾을 수 없습니다. 작업 ID: " + jobId);
        });

        log.info("작업이 성공적으로 제거되었습니다. 작업 ID: {}, PID: {}", jobId, removeProcessJob.pid());
        return removeJavaJob.isEmpty() ? removeProcJob : removeJavaJob;
    }

    public static void runningJobsInfo() {
        if (jobJavaMap.isEmpty() && jobProcMap.isEmpty()) {
            log.info("실행중인 작업이 없습니다.");
        }

        jobJavaMap.entrySet().stream()
                .filter(entry -> ((ProcessHandle) entry.getValue().get("PROCESS")).isAlive())
                .forEach(entry -> log.info("실행중인 Java 작업 - ID : {}, PID : {}", entry.getKey(), ((ProcessHandle) entry.getValue().get("PROCESS")).pid()));


        jobProcMap.entrySet().stream()
                .filter(entry -> ((ProcessHandle) entry.getValue().get("PROCESS")).isAlive())
                .forEach(entry -> log.info("실행중인 Proc 작업 - ID : {}, PID : {}", entry.getKey(), ((ProcessHandle) entry.getValue().get("PROCESS")).pid()));
    }

    public static Map<String, Map<String, Object>> cleanupJobs() {
        Map<String, Map<String, Object>> removeJobInfo = new HashMap<>();

        if (!jobJavaMap.isEmpty()) { // Java 작업 정리
            jobJavaMap.forEach((jobId, value) -> {
                if (!((ProcessHandle) value.get("PROCESS")).isAlive()) {
                    Map<String, Object> jobInfo = jobJavaMap.remove(jobId);
                    jobInfo.remove("PROCESS");
                    removeJobInfo.put(jobId, jobInfo);
                    log.info("종료된 Java 작업 정리 - 작업 ID: {}", jobId);
                }
            });
        }

        if (!jobProcMap.isEmpty()) { // Proc 작업 정리
            jobProcMap.forEach((jobId, value) -> {
                if (!((ProcessHandle) value.get("PROCESS")).isAlive()) {
                    Map<String, Object> jobInfo = jobProcMap.remove(jobId);
                    jobInfo.remove("PROCESS");
                    removeJobInfo.put(jobId, jobInfo);
                    log.info("종료된 Proc 작업 정리 - 작업 ID: {}", jobId);
                }
            });
        }

        return removeJobInfo;
    }

    public static synchronized boolean getProcJobInfo(String jobId) {
        return jobProcMap.containsKey(jobId);
    }

    public static synchronized boolean getJavaJobInfo(String jobId) {
        return jobJavaMap.containsKey(jobId);
    }
}