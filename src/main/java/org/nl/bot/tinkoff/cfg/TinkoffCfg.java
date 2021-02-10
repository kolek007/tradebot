package org.nl.bot.tinkoff.cfg;

import org.nl.bot.api.BotManager;
import org.nl.bot.api.Interval;
import org.nl.bot.api.strategies.StrategiesFactory;
import org.nl.bot.sandbox.SandboxAdapter;
import org.nl.bot.tinkoff.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import ru.tinkoff.invest.openapi.OpenApi;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Configuration
public class TinkoffCfg {
    @Nonnull
    @Value("${org.nl.ssoToken}")
    private String ssoToken;
    @Nonnull
    @Value("#{'${org.nl.tickers}'.split(',')}")
    private String[] tickers;
    @Nonnull
    @Value("#{'${org.nl.candleIntervals}'.split(',')}")
    private String[] candleIntervals;
    @Value("${org.nl.sandbox}")
    private boolean sandbox;

    @Bean(destroyMethod = "destroy")
    OpenApiFactory openApiFactory() {
        return new OpenApiFactory(ssoToken, sandbox, threadPool());
    }

    @Bean
    OpenApi openApi() {
        return openApiFactory().createConnection();
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    TinkoffAdapter tinkoffAdapter() {
        return new TinkoffAdapter(openApi(), beansConverter(), tickerFigiMapping(), botManager(), tkfSubscriber(), ordersManager());
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    SandboxAdapter sandboxAdapter() {
        return new SandboxAdapter(tinkoffAdapter(), threadPool());
    }

    @Bean
    TinkoffSubscriber tkfSubscriber() {
        return new TinkoffSubscriber(threadPool(), beansConverter());
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    OrdersManager ordersManager() {
        return new OrdersManager(beansConverter(), openApi());
    }

    @Bean
    TickerFigiMapping tickerFigiMapping() {
        return new TickerFigiMapping();
    }

    @Bean
    BeansConverter beansConverter() {
        return new BeansConverter(tickerFigiMapping());
    }

    @Bean
    BotManager botManager() {
        return new BotManager();
    }

    @Bean
    StrategiesFactory strategiesFactory() {
        return new StrategiesFactory();
    }

    @Bean(initMethod = "init")
    TinkoffBotsEntryPoint tinkoffBotsEntryPoint() {
        return new TinkoffBotsEntryPoint(botManager(), strategiesFactory(), sandboxAdapter(), tickers, Arrays.stream(candleIntervals).map(Interval::valueOf).collect(Collectors.toList()));
    }

    @Bean
    Executor threadPool() {
        return Executors.newCachedThreadPool();
    }
}
