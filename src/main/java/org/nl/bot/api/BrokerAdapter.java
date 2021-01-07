package org.nl.bot.api;

import org.reactivestreams.example.unicast.AsyncSubscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public interface BrokerAdapter {

    void subscribe(@Nonnull String ticker, @Nonnull Interval interval, @Nonnull AsyncSubscriber<TickerEvent> listener);

    @Nonnull
    CompletableFuture<PlacedOrder> placeOrder(@Nonnull String ticker,
                                                    @Nonnull Order marketOrder,
                                                    @Nullable String brokerAccountId);

    /**
     * Отзыв лимитной заявки.
     *
     * @param orderId Идентификатор заявки.
     * @param brokerAccountId Идентификатор брокерского счёта.
     *
     * @return Ничего.
     */
    @Nonnull
    CompletableFuture<Void> cancelOrder(@Nonnull String orderId, @Nullable String brokerAccountId);

}
