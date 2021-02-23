package org.nl.util;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.EventListener;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.sandbox.beans.PlacedOrderSbx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BrokerMock implements BrokerAdapter {
    @Getter
    @Nonnull
    private final List<PlacedOrder> orders = new ArrayList<>();

    @Nonnull
    private final Map<TickerWithInterval, EventListener<CandleEvent>> botSubscriptions = new ConcurrentHashMap<>();

    public void cleanUp() {
        orders.clear();
        botSubscriptions.clear();;
    }

    public void sendCandle(@Nonnull CandleEvent candleEvent) {
        EventListener<CandleEvent> candleEventEventListener = botSubscriptions.get(new TickerWithInterval(candleEvent.getCandle().getTicker(), candleEvent.getCandle().getInterval()));

        if (candleEventEventListener != null) {
            candleEventEventListener.onEvent(candleEvent);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(@Nonnull String botId, @Nonnull String ticker, @Nonnull Order marketOrder, @Nullable String brokerAccountId) {
        CompletableFuture<PlacedOrder> future = new CompletableFuture<>();
        final PlacedOrderSbx placedOrder = PlacedOrderSbx.builder()
                .ticker(ticker)
                .status(Status.New)
                .id(UUID.randomUUID().toString())
                .lots(marketOrder.getLots())
                .operation(marketOrder.getOperation())
                .price(marketOrder.getPrice())
                .build();
        log.info("Place order. [order={}]", placedOrder);
        orders.add(placedOrder);
        future.complete(placedOrder);
        return future;
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> cancelOrder(@Nonnull String botId, @Nonnull String orderId, @Nullable String brokerAccountId) {
        return null;
    }

    @Nonnull
    @Override
    public CompletableFuture<Optional<Orderbook>> getOrderbook(@Nonnull String ticker, int depth) {
        return null;
    }

    @Nonnull
    @Override
    public CompletableFuture<Optional<List<Candle>>> getHistoricalCandles(@Nonnull String ticker, @Nonnull OffsetDateTime from, @Nonnull OffsetDateTime to, @Nonnull Interval interval) {
        CompletableFuture<Optional<List<Candle>>> optionalCompletableFuture = new CompletableFuture<>();
        optionalCompletableFuture.complete(Optional.of(new ArrayList<>()));

        return optionalCompletableFuture;
    }

    @Override
    public void subscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener) {
        botSubscriptions.put(instrument,listener);
    }

    @Override
    public void unsubscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instr) {

    }

    @Override
    public void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<OrderbookEvent> listener) {

    }

    @Override
    public void unsubscribeOrderbook(@Nonnull String botId, @Nonnull String ticker) {

    }

    @Override
    public void subscribeOnOrdersUpdate(@Nonnull String botId, @Nonnull EventListener<OrderUpdateEvent> listener) {

    }

    @Override
    public void unsubscribeFromOrdersUpdate(@Nonnull String botId) {

    }
}
