package org.nl.util;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.EventListener;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.sandbox.beans.PlacedOrderSbx;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class BrokerMock implements BrokerAdapter {
    @Getter
    @NotNull
    private final List<PlacedOrder> orders = new ArrayList<>();

    @NotNull
    private final Map<TickerWithInterval, EventListener<CandleEvent>> botSubscriptions = new ConcurrentHashMap<>();

    public void cleanUp() {
        orders.clear();
        botSubscriptions.clear();;
    }

    public void sendCandle(@NotNull CandleEvent candleEvent) {
        EventListener<CandleEvent> candleEventEventListener = botSubscriptions.get(new TickerWithInterval(candleEvent.getCandle().getTicker(), candleEvent.getCandle().getInterval()));

        if (candleEventEventListener != null) {
            candleEventEventListener.onEvent(candleEvent);
        }
    }

    @NotNull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(@NotNull String botId, @NotNull String ticker, @NotNull Order marketOrder, @Nullable String brokerAccountId) {
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

    @NotNull
    @Override
    public CompletableFuture<Void> cancelOrder(@NotNull String botId, @NotNull String orderId, @Nullable String brokerAccountId) {
        return null;
    }

    @NotNull
    @Override
    public CompletableFuture<Optional<Orderbook>> getOrderbook(@NotNull String ticker, int depth) {
        return null;
    }

    @NotNull
    @Override
    public CompletableFuture<Optional<List<Candle>>> getHistoricalCandles(@NotNull String ticker, @NotNull OffsetDateTime from, @NotNull OffsetDateTime to, @NotNull Interval interval) {
        return null;
    }

    @Override
    public void subscribeCandle(@NotNull String botId, @NotNull TickerWithInterval instrument, @NotNull EventListener<CandleEvent> listener) {
        botSubscriptions.put(instrument,listener);
    }

    @Override
    public void unsubscribeCandle(@NotNull String botId, @NotNull TickerWithInterval instr) {

    }

    @Override
    public void subscribeOrderbook(@NotNull String botId, @NotNull String ticker, @NotNull EventListener<OrderbookEvent> listener) {

    }

    @Override
    public void unsubscribeOrderbook(@NotNull String botId, @NotNull String ticker) {

    }

    @Override
    public void subscribeOnOrdersUpdate(@NotNull String botId, @NotNull EventListener<OrderUpdateEvent> listener) {

    }

    @Override
    public void unsubscribeFromOrdersUpdate(@NotNull String botId) {

    }
}
