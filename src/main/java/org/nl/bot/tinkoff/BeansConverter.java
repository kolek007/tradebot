package org.nl.bot.tinkoff;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.*;
import org.nl.bot.tinkoff.beans.OrderbookFromStreamTkf;
import org.nl.bot.tinkoff.beans.OrderbookTkf;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class BeansConverter {
    @Nonnull
    private final TickerFigiMapping tickerFigiMapping;

    @Nonnull
    public CandleInterval candleInterval(@Nonnull Interval interval) {
        switch (interval) {
            case MIN_1:
                return CandleInterval.ONE_MIN;
            case MIN_5:
                return CandleInterval.FIVE_MIN;
            case MIN_30:
                return CandleInterval.HALF_HOUR;
            case HOUR_1:
                return CandleInterval.HOUR;
            case WEEK:
                return CandleInterval.WEEK;
            case MONTH:
                return CandleInterval.MONTH;
            default:
                throw new RuntimeException("Couldn't match candle interval " + interval);
        }
    }

    @Nonnull
    public Interval candleInterval(@Nonnull CandleInterval interval) {
        switch (interval) {
            case ONE_MIN:
                return Interval.MIN_1;
            case FIVE_MIN:
                return Interval.MIN_5;
            case HALF_HOUR:
                return Interval.MIN_30;
            case DAY:
                return Interval.DAY_1;
            case WEEK:
                return Interval.WEEK;
            case MONTH:
                return Interval.MONTH;
            default:
                throw new RuntimeException("Couldn't match candle interval " + interval);
        }
    }

    @Nonnull
    public Candle candle(@Nonnull StreamingEvent.Candle candle) {
        final CandleInterval interval = candle.getInterval();
        return Candle.builder()
                .interval(candleInterval(interval))
                .ticker(tickerFigiMapping.getTicker(candle.getFigi()))
                .openPrice(candle.getOpenPrice())
                .closingPrice(candle.getClosingPrice())
                .highestPrice(candle.getHighestPrice())
                .lowestPrice(candle.getLowestPrice())
                .dateTime(candle.getDateTime())
                .tradingValue(candle.getTradingValue())
                .build();
    }

    @Nonnull
    public Order order(@Nonnull ru.tinkoff.invest.openapi.models.orders.Order order) {
        return new Order(order.requestedLots, operation(order.operation), order.price);
    }

    @Nonnull
    public LimitOrder limitOrder(@Nonnull Order order) {
        return new LimitOrder(order.getLots(), operation(order.getOperation()), order.getPrice());
    }

    @Nonnull
    public CompletableFuture<PlacedOrder> placedOrderFuture(@Nonnull CompletableFuture<ru.tinkoff.invest.openapi.models.orders.PlacedOrder> future,
                                                            @Nonnull String ticker) {
        return new CompletableFuture<PlacedOrder>() {
            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public PlacedOrder get() throws InterruptedException, ExecutionException {
                return placedOrder(future.get(), ticker);
            }

            @Override
            public PlacedOrder get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return placedOrder(future.get(timeout, unit), ticker);
            }

            @Override
            public PlacedOrder join() {
                return placedOrder(future.join(), ticker);
            }
        };
    }

    @Nullable
    public MoneyAmount moneyAmount(@Nullable ru.tinkoff.invest.openapi.models.MoneyAmount moneyAmount) {
        if(moneyAmount == null) return null;
        return new MoneyAmount(currency(moneyAmount.currency), moneyAmount.value);
    }

    @Nonnull
    public Currency currency(@Nonnull ru.tinkoff.invest.openapi.models.Currency currency) {
        switch (currency) {
            case EUR:
                return Currency.EUR;
            case USD:
                return Currency.USD;
            case RUB:
                return Currency.RUB;
            default:
                throw new RuntimeException("Couldn't match currency " + currency);
        }
    }

    @Nonnull
    public Operation operation(@Nonnull ru.tinkoff.invest.openapi.models.orders.Operation operation) {
        switch (operation) {
            case Buy:
                return Operation.Buy;
            case Sell:
                return Operation.Sell;
            default:
                throw new RuntimeException("Couldn't match operation " + operation);
        }
    }

    @Nonnull
    public ru.tinkoff.invest.openapi.models.orders.Operation operation(@Nonnull Operation operation) {
        switch (operation) {
            case Buy:
                return ru.tinkoff.invest.openapi.models.orders.Operation.Buy;
            case Sell:
                return ru.tinkoff.invest.openapi.models.orders.Operation.Sell;
            default:
                throw new RuntimeException("Couldn't match operation " + operation);
        }
    }

    @Nonnull
    public Status status(@Nonnull ru.tinkoff.invest.openapi.models.orders.Status status) {
        switch (status) {
            case Cancelled:
            case PendingCancel:
                return Status.Cancelled;
            case New:
            case PendingNew:
                return Status.New;
            case Rejected:
                return Status.Rejected;
            default:
                throw new RuntimeException("Couldn't match status " + status);
        }
    }

    @Nonnull
    public PlacedOrder placedOrder(@Nonnull ru.tinkoff.invest.openapi.models.orders.PlacedOrder order, @Nonnull String ticker) {
        return PlacedOrder.builder()
                .id(order.id)
                .ticker(ticker)
                .commission(moneyAmount(order.commission))
                .executedLots(order.executedLots)
                .message(order.message)
                .requestedLots(order.requestedLots)
                .rejectReason(order.rejectReason)
                .status(status(order.status))
                .build();
    }

    @Nonnull
    public CandleEvent candleEvent(StreamingEvent.Candle event) {
        final CandleEvent.CandleEventBuilder builder = CandleEvent.builder();
        builder.candle(Candle.builder()
                .ticker(tickerFigiMapping.getTicker(event.getFigi()))
                .interval(candleInterval(event.getInterval()))
                .openPrice(event.getOpenPrice())
                .closingPrice(event.getClosingPrice())
                .highestPrice(event.getHighestPrice())
                .lowestPrice(event.getLowestPrice())
                .dateTime(event.getDateTime())
                .tradingValue(event.getTradingValue())
                .build());
        return builder.build();
    }

    @Nonnull
    public CompletableFuture<Optional<Orderbook>> orderbook(CompletableFuture<Optional<ru.tinkoff.invest.openapi.models.market.Orderbook>> future) {
        return new CompletableFuture<Optional<Orderbook>>() {
            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public Optional<Orderbook> get() throws InterruptedException, ExecutionException {
                return orderbook(future.get());
            }

            @Override
            public Optional<Orderbook> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return orderbook(future.get(timeout, unit));
            }

            @Override
            public Optional<Orderbook> join() {
                return orderbook(future.join());
            }
        };
    }

    private Optional<Orderbook> orderbook(Optional<ru.tinkoff.invest.openapi.models.market.Orderbook> orderbook) {
        return orderbook.map(value -> new OrderbookTkf(value, tickerFigiMapping));
    }

    private Orderbook orderbook(@Nonnull StreamingEvent.Orderbook orderbook) {
        return new OrderbookFromStreamTkf(orderbook, tickerFigiMapping);
    }

    @Nonnull
    public OrderbookEvent orderbookEvent(@Nonnull StreamingEvent.Orderbook event) {
        return new OrderbookEvent(orderbook(event));
    }
}
