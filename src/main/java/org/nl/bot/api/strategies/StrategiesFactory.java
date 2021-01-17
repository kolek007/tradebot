package org.nl.bot.api.strategies;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Instrument;
import org.nl.bot.api.Interval;
import org.nl.bot.api.Strategy;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@RequiredArgsConstructor
public class StrategiesFactory {
    @Nonnull
    private final BrokerAdapter adapter;

    public Strategy createAbsorptionStrategy(@Nonnull String ticker, @Nonnull Interval interval) {
        final ArrayList<Instrument> instruments = new ArrayList<>();
        instruments.add(new Instrument(ticker, interval));
        return new AbsorptionStrategy(instruments, adapter);
    }
}
