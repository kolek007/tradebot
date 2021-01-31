package org.nl.bot.api;

import org.reactivestreams.Subscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BrokerAdapter {

    void subscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener);

    void unsubscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument);

    void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<CandleEvent> listener);

    void unsubscribeOrderbook(@Nonnull String botId, @Nonnull String ticker);

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

    @Nonnull
    CompletableFuture<Optional<Orderbook>> getOrderbook(
            @Nonnull String ticker,
            int depth
    );

}
