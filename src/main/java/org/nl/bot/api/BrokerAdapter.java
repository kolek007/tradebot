package org.nl.bot.api;

import org.reactivestreams.Subscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public interface BrokerAdapter {

    void subscribe(@Nonnull String botId, @Nonnull Instrument instrument, @Nonnull Subscriber<TickerEvent> listener);

    void unsubscribe(@Nonnull String botId, @Nonnull Instrument instrument);

    @Nonnull
    CompletableFuture<PlacedOrder> placeOrder(
            @Nonnull String botId,
            @Nonnull String ticker,
            @Nonnull Order marketOrder,
            @Nullable String brokerAccountId);

    /**
     * Отзыв лимитной заявки.
     *
     * @param orderId         Идентификатор заявки.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * @return Ничего.
     */
    @Nonnull
    CompletableFuture<Void> cancelOrder(
            @Nonnull String botId,
            @Nonnull String orderId,
            @Nullable String brokerAccountId
    );

}
