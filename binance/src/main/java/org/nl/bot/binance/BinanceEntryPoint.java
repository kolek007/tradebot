package org.nl.bot.binance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nl.StrategyConfig;
import org.nl.bot.api.*;
import org.nl.bot.api.strategies.StrategiesFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Slf4j
public class BinanceEntryPoint {
    @Nonnull
    BotManager botManager;
    @Nonnull
    StrategiesFactory strategiesFactory;
    @Nonnull
    BrokerAdapter adapter;
    @Nonnull
    StrategyConfig[] strategies;
    @Nonnull
    Executor executor;

    public void init() throws Exception {
        executor.execute(() -> {
            for (StrategyConfig strategy : strategies) {
                String[] tickers = strategy.getTickers().split(",");
                Interval[] intervals = Arrays.stream(strategy.getIntervals().split(",")).map(Interval::valueOf).toArray(Interval[]::new);
                BigDecimal amount = BigDecimal.valueOf(strategy.getWallet().getAmount());
                Strategy strat = null;
                try {
                    strat = strategiesFactory.createStrategy(strategy.getName(),
                            tickers,
                            intervals,
                            WalletImpl.builder().amount(amount).initialAmount(amount).build(),
                            adapter);
                    botManager.run(strat);
                } catch (Exception e) {
                    log.error("Exception starting strategy", e);
                }

            }
        });
    }
}
