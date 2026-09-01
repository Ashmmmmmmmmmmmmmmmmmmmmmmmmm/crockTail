package com.example.crocktail.service;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class RetryHelper {

    /**
     * 执行一个可能失败的操作，失败后重试指定次数，每次重试之间等待一小段时间
     */
    public <T> T withRetry(Supplier<T> action, int maxRetries, long delayMillis) {
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                lastError = e;
                System.out.println("Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        throw lastError;
    }
}