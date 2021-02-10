package org.nl.bot.api.strategies;

import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Interval;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;

import javax.annotation.Nonnull;

public class StrategiesFactory {

    public AbsorptionStrategy createAbsorptionStrategy(@Nonnull String ticker, @Nonnull Interval interval, @Nonnull Wallet wallet, @Nonnull BrokerAdapter adapter) {
        return new AbsorptionStrategy(new TickerWithInterval(ticker, interval), adapter, wallet);
    }
}
