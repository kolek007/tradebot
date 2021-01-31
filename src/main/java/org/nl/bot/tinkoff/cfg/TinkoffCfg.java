package org.nl.bot.tinkoff.cfg;

import org.nl.bot.api.BotManager;
import org.nl.bot.api.strategies.StrategiesFactory;
import org.nl.bot.sandbox.SandboxAdapter;
import org.nl.bot.tinkoff.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;

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
        return new OpenApiFactory(ssoToken, sandbox);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    TinkoffAdapter tinkoffAdapter() {
        return new TinkoffAdapter(openApiFactory().createConnection(), beansConverter(), tickerFigiMapping(), botManager(), subscriber());
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    SandboxAdapter sandboxAdapter() {
        return new SandboxAdapter(tinkoffAdapter());
    }

    @Bean
    TinkoffSubscriber subscriber() {
        return new TinkoffSubscriber(Executors.newSingleThreadExecutor(), beansConverter());
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
        return new StrategiesFactory(tinkoffAdapter());
    }
}
