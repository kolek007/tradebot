package org.nl.bot.tinkoff;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.*;
import org.reactivestreams.example.unicast.AsyncSubscriber;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class TinkoffAdapter implements BrokerAdapter {
    @Override
    public void subscribe(@NotNull String ticker, @NotNull Interval interval, @NotNull AsyncSubscriber<TickerEvent> listener) {

    }

    @Nonnull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(@NotNull String ticker, @NotNull Order marketOrder, @Nullable String brokerAccountId) {
        return null;
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> cancelOrder(@NotNull String orderId, @Nullable String brokerAccountId) {
        return null;
    }
}
