package org.nl.bot.api.strategies;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Interval;
import org.nl.bot.api.Strategy;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@RequiredArgsConstructor
public class StrategiesFactory {
    @Nonnull
    private final BrokerAdapter adapter;

    public Strategy createAbsorptionStrategy(@Nonnull String ticker, @Nonnull Interval interval) {
        final ArrayList<TickerWithInterval> instruments = new ArrayList<>();
        instruments.add(new TickerWithInterval(ticker, interval));
        return new AbsorptionStrategy(instruments, adapter);
    }
}
