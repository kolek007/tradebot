package org.nl.cfg;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nl.StartupConfig;
import org.nl.bot.api.BotManager;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.strategies.StrategiesFactory;
import org.nl.bot.binance.BinanceAdapter;
import org.nl.bot.binance.BinanceEntryPoint;
import org.nl.bot.binance.beans.BeansConverter;
import org.nl.bot.sandbox.SandboxAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class BinanceBotCfg {
    @Value("${org.nl.startup.config}")
    private String startupConfigPath;

    @Bean
    BinanceApiWebSocketClient apiWebSocketClient(@Nonnull StartupConfig startupConfig) {
        return BinanceApiClientFactory.newInstance(startupConfig.getBroker().getApiKey(),
                startupConfig.getBroker().getSecret()).newWebSocketClient();
    }

    @Bean
    BinanceApiRestClient apiRestClient(@Nonnull StartupConfig startupConfig) {
        return BinanceApiClientFactory.newInstance(startupConfig.getBroker().getApiKey(),
                startupConfig.getBroker().getSecret()).newRestClient();
    }

    @Bean
    BinanceApiAsyncRestClient asyncRestClient(@Nonnull StartupConfig startupConfig) {
        return BinanceApiClientFactory.newInstance(startupConfig.getBroker().getApiKey(),
                startupConfig.getBroker().getSecret()).newAsyncRestClient();
    }

    @Bean(destroyMethod = "destroy")
    BinanceAdapter binanceAdapter(@Nonnull BinanceApiWebSocketClient webSocketClient,
                                  @Nonnull BinanceApiRestClient apiRestClient,
                                  @Nonnull BinanceApiAsyncRestClient asyncRestClient,
                                  @Nonnull BotManager botManager,
                                  @Nonnull BeansConverter beansConverter,
                                  @Nonnull @Qualifier("BinanceThreadPool") Executor executor) {
        return new BinanceAdapter(webSocketClient, apiRestClient, asyncRestClient, botManager, beansConverter, executor);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    SandboxAdapter sandboxAdapter(@Nonnull BinanceAdapter adapter) {
        return new SandboxAdapter(adapter, threadPool());
    }

    @Bean
    BotManager botManager() {
        return new BotManager();
    }

    @Bean
    BeansConverter beansConverter() {
        return new BeansConverter();
    }

    @Bean
    StartupConfig startupConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Paths.get(startupConfigPath).toFile(), StartupConfig.class);
    }

    @Bean
    StrategiesFactory strategiesFactory() {
        return new StrategiesFactory();
    }

    @Bean(initMethod = "init")
    BinanceEntryPoint entryPoint(@Nonnull StartupConfig startupConfig, @Nonnull BinanceAdapter binanceAdapter) {
        BrokerAdapter adapter = startupConfig.getBroker().isSandbox() ? sandboxAdapter(binanceAdapter) : binanceAdapter;
        return new BinanceEntryPoint(botManager(), strategiesFactory(), adapter, startupConfig.getStrategies(), threadPool());
    }

    @Bean("BinanceThreadPool")
    @Primary
    Executor threadPool() {
        return Executors.newCachedThreadPool();
    }

}
