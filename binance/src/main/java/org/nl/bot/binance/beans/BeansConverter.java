package org.nl.bot.binance.beans;

import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.OrderBook;
import org.nl.bot.api.CandleEvent;
import org.nl.bot.api.Interval;
import org.nl.bot.api.OrderbookEvent;
import org.nl.bot.api.Status;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.impl.CandleImpl;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BeansConverter {

    @Nonnull
    public CandlestickInterval candleInterval(@Nonnull Interval interval) {
        switch (interval) {
            case MIN_1:
                return CandlestickInterval.ONE_MINUTE;
            case MIN_5:
                return CandlestickInterval.FIVE_MINUTES;
            case MIN_30:
                return CandlestickInterval.HALF_HOURLY;
            case HOUR_1:
                return CandlestickInterval.HOURLY;
            case WEEK:
                return CandlestickInterval.WEEKLY;
            case MONTH:
                return CandlestickInterval.MONTHLY;
            default:
                throw new RuntimeException("Couldn't match candle interval " + interval);
        }
    }

    @Nonnull
    public Interval candleInterval(@Nonnull CandlestickInterval interval) {
        switch (interval) {
            case ONE_MINUTE:
                return Interval.MIN_1;
            case FIVE_MINUTES:
                return Interval.MIN_5;
            case HALF_HOURLY:
                return Interval.MIN_30;
            case HOURLY:
                return Interval.HOUR_1;
            case WEEKLY:
                return Interval.WEEK;
            case MONTHLY:
                return Interval.MONTH;
            default:
                throw new RuntimeException("Couldn't match candle interval " + interval);
        }
    }

    @Nonnull
    public Interval candleInterval(@Nonnull String intervalId) {

        if (CandlestickInterval.ONE_MINUTE.getIntervalId().equals(intervalId))
            return Interval.MIN_1;
        if (CandlestickInterval.FIVE_MINUTES.getIntervalId().equals(intervalId))
            return Interval.MIN_5;
        if (CandlestickInterval.HALF_HOURLY.getIntervalId().equals(intervalId))
            return Interval.MIN_30;
        if (CandlestickInterval.HOURLY.getIntervalId().equals(intervalId))
            return Interval.HOUR_1;
        if (CandlestickInterval.WEEKLY.getIntervalId().equals(intervalId))
            return Interval.WEEK;
        if (CandlestickInterval.MONTHLY.getIntervalId().equals(intervalId))
            return Interval.MONTH;

        throw new RuntimeException("Couldn't match candle interval " + intervalId);

    }

    @Nonnull
    public Status status(@Nonnull OrderStatus orderStatus) {
        switch (orderStatus) {
            case NEW:
            case PARTIALLY_FILLED:
            case FILLED:
                return Status.New;
            case CANCELED:
            case PENDING_CANCEL:
                return Status.Cancelled;
            case REJECTED:
            case EXPIRED:
                return Status.Rejected;
        }
        throw new RuntimeException("Couldn't recognize status " + orderStatus);
    }


    @Nonnull
    public CandleEvent candleEvent(CandlestickEvent event) {
        final CandleEvent.CandleEventBuilder builder = CandleEvent.builder();
        builder.candle(CandleImpl.builder()
                .ticker(event.getSymbol())
                .interval(candleInterval(event.getIntervalId()))
                .openPrice(new BigDecimal(event.getOpen()))
                .closingPrice(new BigDecimal(event.getClose()))
                .highestPrice(new BigDecimal(event.getHigh()))
                .lowestPrice(new BigDecimal(event.getLow()))
                .dateTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getCloseTime()), ZoneId.systemDefault()))
                .tradingValue(BigDecimal.valueOf(event.getNumberOfTrades()))
                .build());
        return builder.build();
    }

    @Nonnull
    public Candle candle(@Nonnull Candlestick cs, @Nonnull String ticker, @Nonnull Interval interval) {
        return CandleImpl.builder()
                .ticker(ticker)
                .interval(interval)
                .openPrice(new BigDecimal(cs.getOpen()))
                .closingPrice(new BigDecimal(cs.getClose()))
                .highestPrice(new BigDecimal(cs.getHigh()))
                .lowestPrice(new BigDecimal(cs.getLow()))
                .dateTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(cs.getCloseTime()), ZoneId.systemDefault()))
                .tradingValue(BigDecimal.valueOf(cs.getNumberOfTrades()))
                .build();
    }

    @Nonnull
    public CompletableFuture<Optional<Orderbook>> orderbookFuture(@Nonnull OrderBook orderbook, @Nonnull String ticker) {
        return new CompletableFuture<Optional<Orderbook>>() {
            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Optional<Orderbook> get() {
                return orderbook(orderbook, ticker);
            }

            @Override
            public Optional<Orderbook> get(long timeout, TimeUnit unit) {
                return orderbook(orderbook, ticker);
            }

            @Override
            public Optional<Orderbook> join() {
                return orderbook(orderbook, ticker);
            }
        };
    }

    private Optional<Orderbook> orderbook(@Nonnull OrderBook orderbook, @Nonnull String ticker) {
        return Optional.of(OrderbookImpl.builder()
                .bids(orderbook.getBids().stream().map(e -> new Orderbook.Item(new BigDecimal(e.getPrice()), new BigDecimal(e.getQty()))).collect(Collectors.toList()))
                .asks(orderbook.getAsks().stream().map(e -> new Orderbook.Item(new BigDecimal(e.getPrice()), new BigDecimal(e.getQty()))).collect(Collectors.toList()))
                .ticker(ticker)
                .depth(orderbook.getAsks().size())
                .build());
    }

    private Orderbook orderbook(@Nonnull DepthEvent orderbook) {
        return OrderbookImpl.builder()
                .bids(orderbook.getBids().stream().map(e -> new Orderbook.Item(new BigDecimal(e.getPrice()), new BigDecimal(e.getQty()))).collect(Collectors.toList()))
                .asks(orderbook.getAsks().stream().map(e -> new Orderbook.Item(new BigDecimal(e.getPrice()), new BigDecimal(e.getQty()))).collect(Collectors.toList()))
                .ticker(orderbook.getSymbol())
                .depth(orderbook.getAsks().size())
                .build();
    }

    @Nonnull
    public OrderbookEvent orderbookEvent(@Nonnull DepthEvent event) {
        return new OrderbookEvent(orderbook(event));
    }
}
