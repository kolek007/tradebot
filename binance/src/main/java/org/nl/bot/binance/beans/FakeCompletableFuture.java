package org.nl.bot.binance.beans;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@ToString
public class FakeCompletableFuture<T> extends CompletableFuture<T> {
    private final T result;

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result;
    }

    @Override
    public T join() {
        return result;
    }
}
