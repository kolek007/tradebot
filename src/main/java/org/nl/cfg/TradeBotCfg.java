package org.nl.cfg;

import org.nl.TickerListener;
import org.nl.TradingParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
public class TradeBotCfg {

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

    @Bean(initMethod = "init", destroyMethod = "destroy")
    TickerListener getTickerListener() {
        return new TickerListener(getTradingParameters());
    }

    @Bean
    TradingParameters getTradingParameters() {
        return TradingParameters.fromProgramArgs(ssoToken, tickers, candleIntervals, sandbox);
    }
}
