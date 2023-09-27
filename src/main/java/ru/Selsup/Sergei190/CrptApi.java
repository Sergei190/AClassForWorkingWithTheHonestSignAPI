package ru.Selsup.Sergei190;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Duration requestInterval;
    private final Lock lock;
    private final Map<Instant, Integer> requestCounts;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestInterval = Duration.ofMillis(timeUnit.toMillis(1));
        this.lock = new ReentrantLock();
        this.requestCounts = new ConcurrentHashMap<>();
    }

    public void createDocument(Object document, String signature) throws InterruptedException {
        lock.lock();
        try {
            removeExpiredRequestCounts();
            if (isRequestLimitExceeded()) {
                System.out.println("Request limit exceeded. Request blocked.");
                return;
            }
            incrementRequestCount();
            // Perform the document creation and signature processing here
            System.out.println("Document created and signed successfully");
        } finally {
            lock.unlock();
        }
    }

    private void removeExpiredRequestCounts() {
        Instant expirationThreshold = Instant.now().minus(requestInterval);
        requestCounts.entrySet().removeIf(entry -> entry.getKey().isBefore(expirationThreshold));
    }

    private boolean isRequestLimitExceeded() {
        int totalRequests = requestCounts.values().stream().mapToInt(Integer::intValue).sum();
        return totalRequests >= requestLimit;
    }

    private void incrementRequestCount() {
        Instant now = Instant.now();
        requestCounts.merge(now, 1, Integer::sum);
    }
}
