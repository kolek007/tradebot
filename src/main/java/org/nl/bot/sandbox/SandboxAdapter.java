package org.nl.bot.sandbox;

import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.*;
import org.nl.bot.api.Currency;
import org.nl.bot.api.EventListener;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.sandbox.beans.PlacedOrderSbx;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
public class SandboxAdapter implements BrokerAdapter {
    public static final BigDecimal COMMISSION = BigDecimal.valueOf(0.002);
    @Nonnull
    private final BrokerAdapter adapter;

    @Nonnull
    private final ConcurrentHashMap<String, Pair<Order, PlacedOrderSbx>> created = new ConcurrentHashMap<>();
    @Nonnull
    private final ConcurrentHashMap<String, PlacedOrderSbx> completed = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<String, EventListener<OrderUpdateEvent>> botSubscriptions = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<String, String> orders2bot = new ConcurrentHashMap<>();
    @Nonnull
    private final LinkedBlockingQueue<String> processingQueue = new LinkedBlockingQueue<>();

    private volatile boolean interrupt = false;

    @Nonnull
    private final Executor executor = Executors.newSingleThreadExecutor();

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
                    final Pair<Order, PlacedOrderSbx> orderPair = created.get(orderId);
                    if(orderPair == null) {
                        continue;
                    }
                    PlacedOrderSbx placedOrder = orderPair.getValue();
                    Order order = orderPair.getKey();
                    synchronized (Objects.requireNonNull(orderId)) {
                        final CompletableFuture<Optional<Orderbook>> future = getOrderbook(placedOrder.getTicker(), 7);
                        final Optional<Orderbook> orderbook = future.join();
                        List<Orderbook.Item> items = new ArrayList<>();
                        BigDecimal moneyAmount = new BigDecimal(0);
                        if(order.getOperation() == Operation.Buy) {
                            orderbook.ifPresent(ob -> items.addAll(ob.getBids()));
                            for (Orderbook.Item item : items) {
                                if(item.getPrice().compareTo(order.getPrice()) <= 0) {
                                    if(item.getQuantity().intValue() >= (placedOrder.requestedLots - placedOrder.executedLots)) {
                                        placedOrder.executedLots += placedOrder.requestedLots;
                                        placedOrder.requestedLots = 0;
                                        if(placedOrder.commission == null) {
                                            placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                                        }
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(BigDecimal.valueOf(placedOrder.requestedLots)).multiply(COMMISSION));
                                    } else {
                                        placedOrder.executedLots += item.getQuantity().intValue();
                                        placedOrder.requestedLots -= item.getQuantity().intValue();
                                        if(placedOrder.commission == null) {
                                            placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                                        }
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(item.getQuantity()).multiply(COMMISSION));
                                    }
                                }
                            }
                        } else {
                            orderbook.ifPresent(ob -> items.addAll(ob.getAsks()));
                            for (Orderbook.Item item : items) {
                                if(item.getPrice().compareTo(order.getPrice()) >= 0) {
                                    if(item.getQuantity().intValue() >= (placedOrder.requestedLots - placedOrder.executedLots)) {
                                        placedOrder.executedLots += placedOrder.requestedLots;
                                        placedOrder.requestedLots = 0;
                                        if(placedOrder.commission == null) {
                                            placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                                        }
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(BigDecimal.valueOf(placedOrder.requestedLots)).multiply(COMMISSION));
                                    } else {
                                        placedOrder.executedLots += item.getQuantity().intValue();
                                        placedOrder.requestedLots -= item.getQuantity().intValue();
                                        if(placedOrder.commission == null) {
                                            placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                                        }
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(item.getQuantity()).multiply(COMMISSION));
                                    }
                                }
                            }
                        }
                        if(placedOrder.requestedLots != 0) {
                            created.put(orderId, orderPair);
                            try {
                                processingQueue.put(orderId);
                            } catch (InterruptedException e) {
                                log.error("Error on put to queue", e);
                            }
                        } else {
                            final String botId = orders2bot.remove(orderId);
                            if(botId != null) {
                                final EventListener<OrderUpdateEvent> listener = botSubscriptions.get(botId);
                                if (listener != null) {
                                    listener.onEvent(new OrderUpdateEvent(placedOrder));
                                }
                            }
                            completed.put(orderId, placedOrder);
                        }
                    }
                }
            }
        });
        //TODO temp code for testing, remove this
        subscribeCandle("TEST", new TickerWithInterval("AAPL", Interval.MIN_1), element -> log.info("Пришло новое событие из Candle API\n {}", element));
        subscribeOrderbook("TEST", "AAPL", element -> log.info("Пришло новое событие из Orderbook API\n {}", element.getOrderbook()));
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
    public CompletableFuture<PlacedOrder> placeOrder(@Nonnull String botId, @Nonnull String ticker, @Nonnull Order marketOrder, @Nullable String brokerAccountId) {
        CompletableFuture<PlacedOrder> future = new CompletableFuture<>();
        final PlacedOrderSbx placedOrder = PlacedOrderSbx.builder()
                .ticker(ticker)
                .status(Status.New)
                .id(UUID.randomUUID().toString())
                .requestedLots(marketOrder.getLots())
                .operation(marketOrder.getOperation())
                .build();
        String orderId = placedOrder.getId();
        synchronized (Objects.requireNonNull(orderId)) {
            created.put(orderId, new Pair<>(marketOrder, placedOrder));
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
        synchronized (Objects.requireNonNull(orderId)) {
            final Pair<Order, PlacedOrderSbx> pair = created.get(orderId);
            if(pair != null) {
                pair.getValue().status = Status.Cancelled;
                completed.put(orderId, pair.getValue());
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
        synchronized (Objects.requireNonNull(orderId)) {
            orders2bot.put(orderId, botId);
        }
    }

    public void cancelOrder(@Nonnull String orderId) {
        synchronized (Objects.requireNonNull(orderId)) {
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

}
