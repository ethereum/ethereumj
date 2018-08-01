package org.ethereum.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Mikhail Kalinin
 * @since 26.07.2018
 */
public class Futures {

    public static <T> TimeoutFuture<T> timeout(long timeout, TimeUnit unit, String exMessage) {
        return new TimeoutFuture<>(timeout, unit, exMessage);
    }

    public static class TimeoutFuture<T> extends CompletableFuture<T> {

        TimeoutFuture(long timeout, TimeUnit unit, String exMessage) {
            Executors.newSingleThreadScheduledExecutor().schedule(
                    () -> this.completeExceptionally(new TimeoutException(exMessage)), timeout, unit);
        }
    }
}
