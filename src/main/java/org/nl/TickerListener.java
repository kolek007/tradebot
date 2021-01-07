package org.nl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.SandboxOpenApi;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Slf4j
@RequiredArgsConstructor
public class TickerListener {
    @Nonnull
    private final TradingParameters parameters;

    public void init() {
        log.info("INIT STARTED");
        final Logger logger;
        try {
            logger = initLogger();
        } catch (IOException e) {
            log.error("Couldn't init logger", e);
            return;
        }

        final OkHttpOpenApiFactory factory = new OkHttpOpenApiFactory(parameters.ssoToken, logger);
        OpenApi api = null;
        try {

            log.info("Создаём подключение... ");
            if (parameters.sandboxMode) {
                api = factory.createSandboxOpenApiClient(Executors.newSingleThreadExecutor());
                // ОБЯЗАТЕЛЬНО нужно выполнить регистрацию в "песочнице"
                ((SandboxOpenApi) api).getSandboxContext().performRegistration(null).join();
            } else {
                api = factory.createOpenApiClient(Executors.newSingleThreadExecutor());
            }

            final StreamingApiSubscriber listener = new StreamingApiSubscriber(Executors.newSingleThreadExecutor());

            api.getStreamingContext().getEventPublisher().subscribe(listener);

            final List<Order> currentOrders = api.getOrdersContext().getOrders(null).join();
            log.info("Количество текущих заявок: {}", currentOrders.size());
            final Portfolio currentPositions = api.getPortfolioContext().getPortfolio(null).join();
            log.info("Количество текущих позиций: {}", currentPositions.positions.size());

            for (int i = 0; i < parameters.tickers.length; i++) {
                final String ticker = parameters.tickers[i];
                final CandleInterval candleInterval = parameters.candleIntervals[i];

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

            initCleanupProcedure(api);

            final CompletableFuture<Void> result = new CompletableFuture<>();
            result.join();
        } catch (final Exception ex) {
            log.error("Something went wrong", ex);
        } finally {
            if(api != null && !api.hasClosed()) {
                try {
                    api.close();
                } catch (Exception e) {
                    log.error("Something went wrong", e);
                }
            }
        }
        log.info("INIT ENDED");
    }

    private static void initCleanupProcedure(final OpenApi api) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Закрываем соединение... ");
                if (!api.hasClosed()) api.close();
            } catch (final Exception e) {
                log.error("Что-то произошло при закрытии соединения!", e);
            }
        }));
    }

    private static Logger initLogger() throws IOException {
        return Logger.getLogger(TickerListener.class.getName());
    }


    private void destroy() {
        log.info("DESTROY STARTED");

        log.info("DESTROY ENDED");
    }
}
