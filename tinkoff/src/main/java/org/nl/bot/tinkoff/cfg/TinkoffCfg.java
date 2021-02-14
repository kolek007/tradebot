package org.nl.bot.tinkoff.cfg;

import org.nl.StartupConfig;
import org.nl.bot.api.BotManager;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.strategies.StrategiesFactory;
import org.nl.bot.sandbox.SandboxAdapter;
import org.nl.bot.tinkoff.*;
import org.nl.cfg.TradeBotCfg;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.tinkoff.invest.openapi.OpenApi;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class TinkoffCfg {

    @Bean(destroyMethod = "destroy")
    OpenApiFactory openApiFactory(@Nonnull StartupConfig startupConfig) {
        StartupConfig.Broker broker = startupConfig.getBroker();
        return new OpenApiFactory(broker.isSandbox() ? broker.getSandboxSsoToken() : broker.getSsoToken(), broker.isSandbox(), threadPool());
    }

    @Bean
    OpenApi openApi(@Nonnull OpenApiFactory openApiFactory) {
        return openApiFactory.createConnection();
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    TinkoffAdapter tinkoffAdapter(@Nonnull OpenApi openApi) {
        return new TinkoffAdapter(openApi, beansConverter(), tickerFigiMapping(), botManager(), tkfSubscriber(), ordersManager(openApi));
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    SandboxAdapter sandboxAdapter(@Nonnull TinkoffAdapter tinkoffAdapter) {
        return new SandboxAdapter(tinkoffAdapter, threadPool());
    }

    @Bean
    TinkoffSubscriber tkfSubscriber() {
        return new TinkoffSubscriber(threadPool(), beansConverter());
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    OrdersManager ordersManager(@Nonnull OpenApi openApi) {
        return new OrdersManager(beansConverter(), openApi);
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
    TinkoffBotsEntryPoint tinkoffBotsEntryPoint(@Nonnull StartupConfig startupConfig, @Nonnull OpenApi openApi) {
        TinkoffAdapter tinkoffAdapter = tinkoffAdapter(openApi);
        BrokerAdapter adapter = startupConfig.getBroker().isSandbox() ? sandboxAdapter(tinkoffAdapter) : tinkoffAdapter;
        return new TinkoffBotsEntryPoint(botManager(), strategiesFactory(), adapter, startupConfig.getStrategies());
    }

    @Bean
    Executor threadPool() {
        return Executors.newCachedThreadPool();
    }
}
