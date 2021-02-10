package org.nl.bot.tinkoff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.api.beans.PlacedOrder;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
public class TinkoffAdapter implements BrokerAdapter {
    public static final int DEPTH = 7;
    @Nonnull
    private final OpenApi api;
    @Nonnull
    private final BeansConverter beansConverter;
    @Nonnull
    private final TickerFigiMapping tickerFigiMapping;
    @Nonnull
    private final BotManager botManager;
    @Nonnull
    private final TinkoffSubscriber tkfSubscriber;
    @Nonnull
    private final OrdersManager ordersManager;

    public void init() {
        log.info("INIT STARTED");
        api.getStreamingContext().getEventPublisher().subscribe(tkfSubscriber);

        final List<ru.tinkoff.invest.openapi.models.orders.Order> currentOrders = api.getOrdersContext().getOrders(null).join();
        log.info("Количество текущих заявок: {}", currentOrders.size());
        final Portfolio currentPositions = api.getPortfolioContext().getPortfolio(null).join();
        log.info("Количество текущих позиций: {}", currentPositions.positions.size());
        for (Portfolio.PortfolioPosition portfolioPosition : currentPositions.positions) {
            log.info("position: \n{}", portfolioPosition);
        }

        log.info("INIT ENDED");
    }

    public void destroy() {
        log.info("DESTROY STARTED");
        try {
            botManager.destroy();
            tkfSubscriber.destroy();
            final List<ru.tinkoff.invest.openapi.models.orders.Order> currentOrders = api.getOrdersContext().getOrders(null).join();
            log.info("Closing {} orders", currentOrders.size());
            for (ru.tinkoff.invest.openapi.models.orders.Order order : currentOrders) {
                log.info("Closing order \n{}", order);
                api.getOrdersContext().cancelOrder(order.id, null).join();
            }
            final Portfolio currentPositions = api.getPortfolioContext().getPortfolio(null).join();
            log.info("Current positions amount: {}", currentPositions.positions.size());
            for (Portfolio.PortfolioPosition portfolioPosition : currentPositions.positions) {
                log.info("position: \n{}", portfolioPosition);
            }
        } finally {
            if (!api.hasClosed()) {
                try {
                    api.close();
                } catch (Exception e) {
                    log.error("Couldn't close connection.", e);
                }
            }
            log.info("DESTROY ENDED");
        }
    }

    @Override
    public void subscribeCandle(
            @Nonnull String botId,
            @Nonnull TickerWithInterval instr,
            @Nonnull EventListener<CandleEvent> listener
    ) {

        final CandleInterval candleInterval = beansConverter.candleInterval(instr.getInterval());

        final Instrument instrument = getInstrument(instr.getTicker());
        if (instrument == null) return;

        tkfSubscriber.subscribeCandle(botId, instr, listener);
        api.getStreamingContext().sendRequest(StreamingRequest.subscribeCandle(instrument.figi, candleInterval));
    }

    @Override
    public void unsubscribeCandle(
            @Nonnull String botId,
            @Nonnull TickerWithInterval instr
    ) {
        final CandleInterval candleInterval = beansConverter.candleInterval(instr.getInterval());

        final Instrument instrument = getInstrument(instr.getTicker());
        if (instrument == null) return;
        try {
            api.getStreamingContext().sendRequest(StreamingRequest.unsubscribeCandle(instrument.figi, candleInterval));
        } finally {
            tkfSubscriber.unsubscribeCandle(botId, instr);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(
            @Nonnull String botId,
            @Nonnull String ticker,
            @Nonnull Order order,
            @Nullable String brokerAccountId
    ) {
        final CompletableFuture<ru.tinkoff.invest.openapi.models.orders.PlacedOrder> future = api.getOrdersContext().placeLimitOrder(tickerFigiMapping.getFigi(ticker), beansConverter.limitOrder(order), null);
        return placedOrderFuture(future, ticker, botId, order, ordersManager);
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> cancelOrder(@Nonnull String botId, @Nonnull String orderId, @Nullable String brokerAccountId) {
        final CompletableFuture<Void> future = api.getOrdersContext().cancelOrder(orderId, null);
        ordersManager.cancelOrder(orderId);
        return future;
    }

    @Nonnull
    @Override
    public CompletableFuture<Optional<Orderbook>> getOrderbook(@Nonnull String ticker, int depth) {

        return beansConverter.orderbook(api.getMarketContext().getMarketOrderbook(tickerFigiMapping.getFigi(ticker), depth));
    }

    @Override
    public void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<OrderbookEvent> listener) {
        getInstrument(ticker);
        tkfSubscriber.subscribeOrderbook(botId, ticker, listener);
        api.getStreamingContext().sendRequest(StreamingRequest.subscribeOrderbook(tickerFigiMapping.getFigi(ticker), DEPTH));
    }

    @Override
    public void unsubscribeOrderbook(@Nonnull String botId, @Nonnull String ticker) {
        try {
            api.getStreamingContext().sendRequest(StreamingRequest.unsubscribeOrderbook(tickerFigiMapping.getFigi(ticker), DEPTH));
        } finally {
            tkfSubscriber.unsubscribeOrderbook(botId, ticker);
        }
    }

    @Nullable
    private Instrument getInstrument(@Nonnull String ticker) {
        log.info("Ищём по тикеру {}... ", ticker);
        final InstrumentsList instrumentsList = api.getMarketContext().searchMarketInstrumentsByTicker(ticker).join();

        final Optional<Instrument> instrumentOpt = instrumentsList.instruments.stream().findFirst();

        final Instrument instrument;
        if (!instrumentOpt.isPresent()) {
            log.error("Не нашлось инструмента с нужным тикером.");
            return null;
        } else {
            instrument = instrumentOpt.get();
        }

        tickerFigiMapping.put(ticker, instrument.figi);

        log.info("Получаем валютные балансы... ");
        final PortfolioCurrencies portfolioCurrencies = api.getPortfolioContext().getPortfolioCurrencies(null).join();

        final Optional<PortfolioCurrencies.PortfolioCurrency> portfolioCurrencyOpt = portfolioCurrencies.currencies.stream()
                .filter(pc -> pc.currency == instrument.currency)
                .findFirst();

        final PortfolioCurrencies.PortfolioCurrency portfolioCurrency;
        if (!portfolioCurrencyOpt.isPresent()) {
            log.error("Не нашлось нужной валютной позиции.");
            return null;
        } else {
            portfolioCurrency = portfolioCurrencyOpt.get();
            log.info("Нужной валюты {} на счету {}", portfolioCurrency.currency, portfolioCurrency.balance.toPlainString());
        }
        return instrument;
    }

    @Nonnull
    public CompletableFuture<PlacedOrder> placedOrderFuture(@Nonnull CompletableFuture<ru.tinkoff.invest.openapi.models.orders.PlacedOrder> future,
                                                            String botId, @Nonnull String ticker,
                                                            @Nonnull Order order, @Nonnull OrdersManager ordersManager) {
        return new CompletableFuture<PlacedOrder>() {
            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public PlacedOrder get() throws InterruptedException, ExecutionException {
                final PlacedOrder placedOrder = beansConverter.placedOrder(future.get(), order.getPrice(), ticker);
                ordersManager.registerOrder(botId, placedOrder);
                return placedOrder;
            }

            @Override
            public PlacedOrder get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                final PlacedOrder placedOrder = beansConverter.placedOrder(future.get(timeout, unit), order.getPrice(), ticker);
                ordersManager.registerOrder(botId, placedOrder);
                return placedOrder;
            }

            @Override
            public PlacedOrder join() {
                final PlacedOrder placedOrder = beansConverter.placedOrder(future.join(), order.getPrice(), ticker);
                ordersManager.registerOrder(botId, placedOrder);
                return placedOrder;
            }
        };
    }

    @Override
    public void subscribeOnOrdersUpdate(@Nonnull String botId, @Nonnull EventListener<OrderUpdateEvent> listener) {
        ordersManager.subscribeOnOrdersUpdate(botId, listener);
    }

    @Override
    public void unsubscribeFromOrdersUpdate(@Nonnull String botId) {
        ordersManager.unsubscribeFromOrdersUpdate(botId);
    }
}
