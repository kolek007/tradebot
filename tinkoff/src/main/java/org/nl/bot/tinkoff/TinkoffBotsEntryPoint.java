package org.nl.bot.tinkoff;

import lombok.RequiredArgsConstructor;
import org.nl.StrategyConfig;
import org.nl.bot.api.*;
import org.nl.bot.api.strategies.StrategiesFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;

@RequiredArgsConstructor
public class TinkoffBotsEntryPoint {
    @Nonnull
    BotManager botManager;
    @Nonnull
    StrategiesFactory strategiesFactory;
    @Nonnull
    BrokerAdapter adapter;
    @Nonnull
    StrategyConfig[] strategies;

    public void init() throws Exception {
        for (StrategyConfig strategy : strategies) {
            String[] tickers = strategy.getTickers().split(",");
            Interval[] intervals = Arrays.stream(strategy.getIntervals().split(",")).map(Interval::valueOf).toArray(Interval[]::new);
            BigDecimal amount = BigDecimal.valueOf(strategy.getWallet().getAmount());
            Strategy strat = strategiesFactory.createStrategy(strategy.getName(),
                    tickers,
                    intervals,
                    WalletImpl.builder().amount(amount).initialAmount(amount).build(),
                    adapter);
            botManager.run(strat);

        }
    }
}
