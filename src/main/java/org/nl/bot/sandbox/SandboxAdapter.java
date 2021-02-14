package org.nl.bot.sandbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.Currency;
import org.nl.bot.api.EventListener;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.sandbox.beans.PlacedOrderSbx;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

@RequiredArgsConstructor
@Slf4j
public class SandboxAdapter implements BrokerAdapter {
    public static final BigDecimal COMMISSION = BigDecimal.valueOf(0.002);
    @Nonnull
    private final BrokerAdapter adapter;
    @Nonnull
    private final Executor executor;

    @Nonnull
    private final ConcurrentHashMap<String, PlacedOrderSbx> created = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<String, EventListener<OrderUpdateEvent>> botSubscriptions = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<String, String> orders2bot = new ConcurrentHashMap<>();
    @Nonnull
    private final LinkedBlockingQueue<String> processingQueue = new LinkedBlockingQueue<>();

    private volatile boolean interrupt = false;

    public void destroy() {
        interrupt = true;
        botSubscriptions.keySet().forEach(this::unsubscribeFromOrdersUpdate);
    }

    public void init() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (!interrupt) {
                    final String orderId = processingQueue.poll();
                    if(orderId == null) {
                        continue;
                    }
                    final PlacedOrderSbx placedOrder = created.get(orderId);
                    if(placedOrder == null) {
                        continue;
                    }
                    synchronized (orderId) {
                        final CompletableFuture<Optional<Orderbook>> future = getOrderbook(placedOrder.getTicker(), 7);
                        final Optional<Orderbook> orderbook = future.join();
                        List<Orderbook.Item> items = new ArrayList<>();
                        if(placedOrder.getOperation() == Operation.Buy) {
                            orderbook.ifPresent(ob -> items.addAll(ob.getBids()));
                            executeBuy(placedOrder, items);
                        } else {
                            orderbook.ifPresent(ob -> items.addAll(ob.getAsks()));
                            executeSell(placedOrder, items);
                        }
                        if(placedOrder.lots != 0) { //Order updated but not executed completely
                            created.put(orderId, placedOrder);
                            try {
                                processingQueue.put(orderId);
                            } catch (InterruptedException e) {
                                log.error("Error on put to queue", e);
                            }
                        } else { //Order executed completely
                            log.info("Order executed: {}", placedOrder);
                            final String botId = orders2bot.remove(orderId);
                            if(botId != null) {
                                final EventListener<OrderUpdateEvent> listener = botSubscriptions.get(botId);
                                if (listener != null) {
                                    listener.onEvent(new OrderUpdateEvent(placedOrder));
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void executeSell(PlacedOrderSbx placedOrder, List<Orderbook.Item> items) {
        for (Orderbook.Item item : items) {
            if(item.getPrice().compareTo(placedOrder.getPrice()) >= 0) {
                if(item.getQuantity().intValue() >= (placedOrder.lots - placedOrder.executedLots)) {
                    placedOrder.executedLots += placedOrder.lots;
                    placedOrder.lots = 0;
                    if(placedOrder.commission == null) {
                        placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                    }
                    placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(BigDecimal.valueOf(placedOrder.lots)).multiply(COMMISSION));
                } else {
                    placedOrder.executedLots += item.getQuantity().intValue();
                    placedOrder.lots -= item.getQuantity().intValue();
                    if(placedOrder.commission == null) {
                        placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                    }
                    placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(item.getQuantity()).multiply(COMMISSION));
                }
            }
        }
    }

    private void executeBuy(PlacedOrderSbx placedOrder, List<Orderbook.Item> items) {
        for (Orderbook.Item item : items) {
            if(item.getPrice().compareTo(placedOrder.getPrice()) <= 0) {
                if(item.getQuantity().intValue() >= (placedOrder.lots - placedOrder.executedLots)) {
                    placedOrder.executedLots += placedOrder.lots;
                    placedOrder.lots = 0;
                    if(placedOrder.commission == null) {
                        placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                    }
                    placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(BigDecimal.valueOf(placedOrder.lots)).multiply(COMMISSION));
                } else {
                    placedOrder.executedLots += item.getQuantity().intValue();
                    placedOrder.lots -= item.getQuantity().intValue();
                    if(placedOrder.commission == null) {
                        placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                    }
                    placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(item.getQuantity()).multiply(COMMISSION));
                }
            }
        }
    }

    @Override
    public void subscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener) {
        adapter.subscribeCandle(botId, instrument, listener);
    }

    @Override
    public void unsubscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument) {
        adapter.unsubscribeCandle(botId, instrument);
    }

    @Override
    public void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<OrderbookEvent> listener) {
        adapter.subscribeOrderbook(botId, ticker, listener);
    }

    @Override
    public void unsubscribeOrderbook(@Nonnull String botId, @Nonnull String ticker) {
        adapter.unsubscribeOrderbook(botId, ticker);
    }

    @Nonnull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(@Nonnull String botId, @Nonnull String ticker, @Nonnull Order limitOrder, @Nullable String brokerAccountId) {
        CompletableFuture<PlacedOrder> future = new CompletableFuture<>();
        final PlacedOrderSbx placedOrder = PlacedOrderSbx.builder()
                .ticker(ticker)
                .status(Status.New)
                .id(UUID.randomUUID().toString())
                .lots(limitOrder.getLots())
                .operation(limitOrder.getOperation())
                .price(limitOrder.getPrice())
                .build();
        String orderId = placedOrder.getId();
        synchronized (orderId) {
            created.put(orderId, placedOrder);
            registerOrder(botId, orderId);
            try {
                processingQueue.put(orderId);
            } catch (InterruptedException e) {
                log.error("Error on put to queue", e);
            }
            future.complete(placedOrder);
            return future;
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> cancelOrder(@Nonnull String botId, @Nonnull String orderId, @Nullable String brokerAccountId) {
        synchronized (orderId) {
            final PlacedOrderSbx order = created.get(orderId);
            if(order != null) {
                order.status = Status.Cancelled;
                cancelOrder(orderId);
            }
            final CompletableFuture<Void> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<Optional<Orderbook>> getOrderbook(@Nonnull String ticker, int depth) {
        return adapter.getOrderbook(ticker, depth);
    }

    public void registerOrder(@Nonnull String botId, @Nonnull String orderId) {
        synchronized (orderId) {
            orders2bot.put(orderId, botId);
        }
    }

    public void cancelOrder(@Nonnull String orderId) {
        synchronized (orderId) {
            orders2bot.remove(orderId);
        }
    }

    @Override
    public void subscribeOnOrdersUpdate(@Nonnull String botId, @Nonnull EventListener<OrderUpdateEvent> listener) {
        botSubscriptions.put(botId, listener);
    }

    @Override
    public void unsubscribeFromOrdersUpdate(@Nonnull String botId) {
        botSubscriptions.remove(botId);
    }

    @Nonnull
    @Override
    public CompletableFuture<Optional<List<Candle>>> getHistoricalCandles(@Nonnull String ticker, @Nonnull OffsetDateTime from, @Nonnull OffsetDateTime to, @Nonnull Interval interval) {
        return adapter.getHistoricalCandles(ticker, from, to, interval);
    }
}
