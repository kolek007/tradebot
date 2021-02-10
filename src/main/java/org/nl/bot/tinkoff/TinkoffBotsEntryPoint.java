package org.nl.bot.tinkoff;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.BotManager;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Interval;
import org.nl.bot.api.WalletImpl;
import org.nl.bot.api.strategies.AbsorptionStrategy;
import org.nl.bot.api.strategies.StrategiesFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class TinkoffBotsEntryPoint {
    @Nonnull
    BotManager botManager;
    @Nonnull
    StrategiesFactory strategiesFactory;
    @Nonnull
    BrokerAdapter adapter;
    @Nonnull
    String[] tickers;
    @Nonnull
    List<Interval> intervals;

    public void init() {
        for(int i = 0; i < tickers.length; i++) {
            String ticker = tickers[i];
            Interval interval = intervals.get(i);
            AbsorptionStrategy absorptionStrategy = strategiesFactory.createAbsorptionStrategy(ticker,
                    interval,
                    WalletImpl.builder()
                        .amount(BigDecimal.valueOf(1000))
                        .initialAmount(BigDecimal.valueOf(1000))
                        .build(),
                    adapter
            );
            botManager.run(absorptionStrategy);
        }
    }
}
