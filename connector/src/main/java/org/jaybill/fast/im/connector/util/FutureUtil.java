package org.jaybill.fast.im.connector.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Slf4j
public class FutureUtil {
    /**
     * @return remaining time millis
     */
    public static <T> long waiting(List<CompletableFuture<T>> futureList, long timeout, TimeUnit unit, Consumer<T> consumer) {
        long lastTime = System.currentTimeMillis();
        long timeoutMillis = unit.toMillis(timeout);
        long currentTimeoutMillis = timeoutMillis;
        int size = futureList.size();
        log.debug("wait for future size:{}", size);
        for (int i = 0; i < size; i++) {
            if (currentTimeoutMillis < 0) {
                break;
            }
            log.debug("process future index:{}", i);
            var future = futureList.get(i);
            try {
                var ackResult = future.get(currentTimeoutMillis, TimeUnit.MILLISECONDS);
                consumer.accept(ackResult);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("thread be interrupt");
            } catch (ExecutionException e) {
                log.error("push message error, e:", e);
            } catch (TimeoutException e) {
                log.error("push message timeout error, e:", e);
                break;
            }
            currentTimeoutMillis = timeoutMillis - (System.currentTimeMillis() - lastTime);
            lastTime = System.currentTimeMillis();
        }
        return timeoutMillis - (System.currentTimeMillis() - lastTime);
    }
}
