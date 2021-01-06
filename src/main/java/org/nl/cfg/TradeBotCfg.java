package org.nl.cfg;

import org.nl.TickerListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
public class TradeBotCfg {

    @Nonnull
    @Value("${org.nl.ssoToken}")
    private String ssoToken;

    @Bean(initMethod = "init", destroyMethod = "destroy")
    TickerListener getTickerListener() {
        return new TickerListener(ssoToken);
    }
}
