package org.nl.bot.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.CancelOrderResponse;
import com.binance.api.client.domain.event.OrderTradeUpdateEvent;
import com.binance.api.client.domain.market.Candlestick;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.binance.beans.BeansConverter;
import org.nl.bot.binance.beans.FakeCompletableFuture;
import org.nl.bot.binance.beans.PlacedOrderFromEvent;
import org.nl.bot.binance.beans.PlacedOrderImpl;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class BinanceAdapter implements BrokerAdapter {
    @Nonnull
    private final BinanceApiWebSocketClient webSocketClient;
    @Nonnull
    private final BinanceApiRestClient apiRestClient;
    @Nonnull
    private final BinanceApiAsyncRestClient asyncRestClient;
    @Nonnull
    private final BotManager botManager;
    @Nonnull
    private final BeansConverter beansConverter;
    @Nonnull
    Executor executor;

    @Nonnull
    private final Map<InstrumentPerBot, Closeable> candleSubscriptions = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<TickerPerBot, Closeable> orderbookSubscriptions = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<String, Closeable> orderSubscriptions = new ConcurrentHashMap<>();

    @SneakyThrows
    private static void close(Closeable v) {
        v.close();
    }

    @Nonnull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(@Nonnull String botId, @Nonnull String ticker, @Nonnull Order marketOrder, @Nullable String brokerAccountId) {
        NewOrderResponse newOrderResponse = apiRestClient.newOrder(new NewOrder(ticker, marketOrder.getOperation() == Operation.Buy ? OrderSide.BUY : OrderSide.SELL, OrderType.LIMIT, TimeInForce.GTC, String.valueOf(marketOrder.getLots()), marketOrder.getPrice().toPlainString()));
        log.info("Place order {} on ticker {} by bot {}", marketOrder, ticker, botId);
        PlacedOrderImpl placedOrder = new PlacedOrderImpl(newOrderResponse);
        return new FakeCompletableFuture<>(placedOrder);
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> cancelOrder(@Nonnull String botId, @Nonnull String ticker, @Nonnull String orderId, @Nullable String brokerAccountId) {
        CancelOrderResponse cancelOrderResponse = apiRestClient.cancelOrder(new CancelOrderRequest(ticker, orderId));
        log.info("Order {} canceled with response {}", orderId, cancelOrderResponse);
        return new FakeCompletableFuture<>(null);
    }

    @Nonnull
    @Override
    public CompletableFuture<Optional<Orderbook>> getOrderbook(@Nonnull String ticker, int depth) {
        return beansConverter.orderbookFuture(apiRestClient.getOrderBook(ticker, depth), ticker);
    }

    @Nonnull
    @Override
    public CompletableFuture<Optional<List<Candle>>> getHistoricalCandles(@Nonnull String ticker, @Nonnull OffsetDateTime from, @Nonnull OffsetDateTime to, @Nonnull Interval interval) {
        List<Candlestick> candlestickBars = apiRestClient.getCandlestickBars(ticker, beansConverter.candleInterval(interval), 10000, from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli());
        List<Candle> list = candlestickBars.stream().map(cs -> beansConverter.candle(cs, ticker, interval)).collect(Collectors.toList());
        return new FakeCompletableFuture<>(Optional.of(list));
    }

    @Override
    public void subscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener) {
        log.info("Subscribe on candles {} by bot {}", instrument, botId);
        Closeable closeable = webSocketClient.onCandlestickEvent(
                instrument.getTicker().toLowerCase(),
                beansConverter.candleInterval(instrument.getInterval()),
                candlestickEvent -> executor.execute(() -> listener.onEvent(beansConverter.candleEvent(candlestickEvent)))
        );
        candleSubscriptions.put(new InstrumentPerBot(botId, instrument), closeable);
    }

    @Override
    public void unsubscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instr) {
        Closeable closeable = candleSubscriptions.remove(new InstrumentPerBot(botId, instr));
        if(closeable != null) {
            close(closeable);
        }
    }

    @Override
    public void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<OrderbookEvent> listener) {
        log.info("Subscribe on orderbook {} by bot {}", ticker, botId);
        Closeable closeable = webSocketClient.onDepthEvent(
                ticker.toLowerCase(),
                bookTickerEvent -> executor.execute(() -> listener.onEvent(beansConverter.orderbookEvent(bookTickerEvent)))
        );
        orderbookSubscriptions.put(new TickerPerBot(botId, ticker), closeable);
    }

    @Override
    public void unsubscribeOrderbook(@Nonnull String botId, @Nonnull String ticker) {
        Closeable closeable = orderbookSubscriptions.remove(new TickerPerBot(botId, ticker));
        if(closeable != null) {
            close(closeable);
        }
    }

    @Override
    public void subscribeOnOrdersUpdate(@Nonnull String botId, @Nonnull EventListener<OrderUpdateEvent> listener) {
        log.info("Subscribe on orders updates by bot {}", botId);
        Closeable closeable = webSocketClient.onUserDataUpdateEvent(botId, userDataUpdateEvent -> {
            OrderTradeUpdateEvent orderTradeUpdateEvent = userDataUpdateEvent.getOrderTradeUpdateEvent();
            if (orderTradeUpdateEvent != null) {
                executor.execute(() -> listener.onEvent(new OrderUpdateEvent(new PlacedOrderFromEvent(orderTradeUpdateEvent))));
            }
        });
        orderSubscriptions.put(botId, closeable);
    }

    @Override
    public void unsubscribeFromOrdersUpdate(@Nonnull String botId) {
        Closeable closeable = orderSubscriptions.remove(botId);
        if(closeable != null) {
            close(closeable);
        }
    }

    public void destroy() {
        log.info("DESTROY STARTED");
        botManager.destroy();
        candleSubscriptions.values().forEach(BinanceAdapter::close);
        orderbookSubscriptions.values().forEach(BinanceAdapter::close);
        orderSubscriptions.values().forEach(BinanceAdapter::close);
        log.info("DESTROY ENDED");
    }
}
