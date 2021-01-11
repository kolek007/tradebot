package org.nl.bot.tinkoff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.*;
import org.reactivestreams.example.unicast.AsyncSubscriber;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Slf4j
public class TinkoffAdapter implements BrokerAdapter {
    @Nonnull
    private final OpenApi api;
    @Nonnull
    private final BeansConverter beansConverter;
    @Nonnull
    private final TickerFigiMapping tickerFigiMapping;

    public void init() {
        log.info("INIT STARTED");

        final StreamingApiSubscriber listener = new StreamingApiSubscriber(Executors.newSingleThreadExecutor());

        api.getStreamingContext().getEventPublisher().subscribe(listener);

        final List<ru.tinkoff.invest.openapi.models.orders.Order> currentOrders = api.getOrdersContext().getOrders(null).join();
        log.info("Количество текущих заявок: {}", currentOrders.size());
        final Portfolio currentPositions = api.getPortfolioContext().getPortfolio(null).join();
        log.info("Количество текущих позиций: {}", currentPositions.positions.size());

        //TODO temp code for testing, remove this
        subscribe("AAPL", Interval.MIN_1, new AsyncSubscriber<TickerEvent>(Executors.newSingleThreadExecutor()) {
            @Override
            protected boolean whenNext(TickerEvent element) {
                log.info("Пришло новое событие из Streaming API\n {}", element);
                return true;
            }
        });
        log.info("INIT ENDED");
    }

    private void destroy() {
        log.info("DESTROY STARTED");

        log.info("DESTROY ENDED");
    }

    @Override
    public void subscribe(@Nonnull String ticker, @Nonnull Interval interval, @Nonnull AsyncSubscriber<TickerEvent> listener) {

        final CandleInterval candleInterval = beansConverter.candleInterval(interval);

        log.info("Ищём по тикеру {}... ", ticker);
        final InstrumentsList instrumentsList = api.getMarketContext().searchMarketInstrumentsByTicker(ticker).join();

        final Optional<Instrument> instrumentOpt = instrumentsList.instruments.stream().findFirst();

        final Instrument instrument;
        if (!instrumentOpt.isPresent()) {
            log.error("Не нашлось инструмента с нужным тикером.");
            return;
        } else {
            instrument = instrumentOpt.get();
        }

        log.info("Получаем валютные балансы... ");
        final PortfolioCurrencies portfolioCurrencies = api.getPortfolioContext().getPortfolioCurrencies(null).join();

        final Optional<PortfolioCurrencies.PortfolioCurrency> portfolioCurrencyOpt = portfolioCurrencies.currencies.stream()
                .filter(pc -> pc.currency == instrument.currency)
                .findFirst();

        final PortfolioCurrencies.PortfolioCurrency portfolioCurrency;
        if (!portfolioCurrencyOpt.isPresent()) {
            log.error("Не нашлось нужной валютной позиции.");
            return;
        } else {
            portfolioCurrency = portfolioCurrencyOpt.get();
            log.info("Нужной валюты {} на счету {}", portfolioCurrency.currency, portfolioCurrency.balance.toPlainString());
        }

        api.getStreamingContext().sendRequest(StreamingRequest.subscribeCandle(instrument.figi, candleInterval));
    }

    @Nonnull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(@NotNull String ticker, @NotNull Order order, @Nullable String brokerAccountId) {
        final CompletableFuture<ru.tinkoff.invest.openapi.models.orders.PlacedOrder> future = api.getOrdersContext().placeLimitOrder(tickerFigiMapping.getFigi(ticker), beansConverter.limitOrder(order), null);
        return beansConverter.placedOrderFuture(future);
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> cancelOrder(@NotNull String orderId, @Nullable String brokerAccountId) {
        return api.getOrdersContext().cancelOrder(orderId, null);
    }
}
