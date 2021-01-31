package org.nl.bot.sandbox;

import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.*;
import org.reactivestreams.Subscriber;
import org.reactivestreams.example.unicast.AsyncSubscriber;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
public class SandboxAdapter implements BrokerAdapter {
    public static final BigDecimal COMISSION = BigDecimal.valueOf(0.002);
    @Nonnull
    private final BrokerAdapter adapter;

    @Nonnull
    private final ConcurrentHashMap<String, Pair<Order, PlacedOrder>> created = new ConcurrentHashMap<>();
    @Nonnull
    private final ConcurrentHashMap<String, PlacedOrder> completed = new ConcurrentHashMap<>();

    @Nonnull
    private final LinkedBlockingQueue<String> processingQueue = new LinkedBlockingQueue<>();

    private volatile boolean interrupt = false;

    @Nonnull
    private final Executor executor = Executors.newSingleThreadExecutor();

    public void destroy() {
        interrupt = true;
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
                    final Pair<Order, PlacedOrder> orderPair = created.get(orderId);
                    if(orderPair == null) {
                        continue;
                    }
                    PlacedOrder placedOrder = orderPair.getValue();
                    Order order = orderPair.getKey();
                    synchronized (orderId) {
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
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(BigDecimal.valueOf(placedOrder.requestedLots)).multiply(COMISSION));
                                    } else {
                                        placedOrder.executedLots += item.getQuantity().intValue();
                                        placedOrder.requestedLots -= item.getQuantity().intValue();
                                        if(placedOrder.commission == null) {
                                            placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                                        }
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(item.getQuantity()).multiply(COMISSION));
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
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(BigDecimal.valueOf(placedOrder.requestedLots)).multiply(COMISSION));
                                    } else {
                                        placedOrder.executedLots += item.getQuantity().intValue();
                                        placedOrder.requestedLots -= item.getQuantity().intValue();
                                        if(placedOrder.commission == null) {
                                            placedOrder.commission = new MoneyAmount(Currency.USD, BigDecimal.ZERO);
                                        }
                                        placedOrder.commission.value = placedOrder.commission.value.add(item.getPrice().multiply(item.getQuantity()).multiply(COMISSION));
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
                            completed.put(orderId, placedOrder);
                        }
                    }

                }
            }
        });
        //TODO temp code for testing, remove this
        subscribeCandle("TEST", new TickerWithInterval("AAPL", Interval.MIN_1), element -> log.info("Пришло новое событие из Candle API\n {}", element));
        subscribeOrderbook("TEST", "AAPL", element -> log.info("Пришло новое событие из Orderbook API\n {}", element));
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
    public void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<CandleEvent> listener) {
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
        final PlacedOrder placedOrder = PlacedOrder.builder()
                .ticker(ticker)
                .status(Status.New)
                .id(UUID.randomUUID().toString())
                .requestedLots(marketOrder.getLots())
                .operation(marketOrder.getOperation())
                .build();
        created.put(placedOrder.getId(), new Pair<>(marketOrder, placedOrder));
        try {
            processingQueue.put(placedOrder.getId());
        } catch (InterruptedException e) {
            log.error("Error on put to queue", e);
        }
        future.complete(placedOrder);
        return future;
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> cancelOrder(@Nonnull String botId, @Nonnull String orderId, @Nullable String brokerAccountId) {
        synchronized (orderId) {
            final Pair<Order, PlacedOrder> pair = created.get(orderId);
            if(pair != null) {
                pair.getValue().status = Status.Cancelled;
                completed.put(orderId, pair.getValue());
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
}
