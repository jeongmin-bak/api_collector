package com.example.externalurl.component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ConnectionManager {
    private Semaphore connection;
    private final ReentrantLock lock = new ReentrantLock();

    @Value("${agent.execute.connection_count}")
    private int cnnCnt;

    @PostConstruct
    public void init() {
        this.connection = new Semaphore(cnnCnt, true);
    }
    public ConnectionManager() {}

    public boolean acquireConnection(int requiredConnection) {
        lock.lock();
        try {
            if (connection.tryAcquire(requiredConnection)) {
                log.info("커넥션 {} 개 획득 성공. 남은 커넥션: {}", requiredConnection, connection.availablePermits());
                return true;
            } else {
                log.info("커넥션 획득 실패. 요청: {}, 가용: {}", requiredConnection, connection.availablePermits());
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void releaseConnection(int releasingConnections) {
        lock.lock();
        try {
            connection.release(releasingConnections);
            log.info("커넥션 {} 개 반환 완료. 현재 가용 커넥션: {}", releasingConnections, connection.availablePermits());
        } finally {
            lock.unlock();
        }
    }
}