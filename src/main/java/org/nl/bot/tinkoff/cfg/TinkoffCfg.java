package org.nl.bot.tinkoff.cfg;

import org.nl.bot.tinkoff.BeansConverter;
import org.nl.bot.tinkoff.OpenApiFactory;
import org.nl.bot.tinkoff.TickerFigiMapping;
import org.nl.bot.tinkoff.TinkoffAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

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
    OpenApiFactory getOpenApiFactory() {
        return new OpenApiFactory(ssoToken, sandbox);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    TinkoffAdapter getTinkoffAdapter() {
        return new TinkoffAdapter(getOpenApiFactory().createConnection(), getBeansConverter(), getTickerFigiMapping());
    }

    @Bean
    TickerFigiMapping getTickerFigiMapping() {
        return new TickerFigiMapping();
    }

    @Bean
    BeansConverter getBeansConverter() {
        return new BeansConverter(getTickerFigiMapping());
    }
}
