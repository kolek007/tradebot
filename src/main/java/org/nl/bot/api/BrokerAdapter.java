package org.nl.bot.api;

import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.api.subscribers.CandleSubscriber;
import org.nl.bot.api.subscribers.OrderbookSubscriber;
import org.nl.bot.api.subscribers.OrdersSubscriber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BrokerAdapter extends CandleSubscriber, OrderbookSubscriber, OrdersSubscriber {

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

    @Nonnull
    CompletableFuture<Optional<List<Candle>>> getHistoricalCandles(@Nonnull String ticker,
                                                                   @Nonnull OffsetDateTime from,
                                                                   @Nonnull OffsetDateTime to,
                                                                   @Nonnull Interval interval);

}
