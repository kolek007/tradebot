package org.nl.bot.api.strategies;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@RequiredArgsConstructor
public class StrategiesFactory {
    @Nonnull
    private final BrokerAdapter adapter;

    public AbsorptionStrategy createAbsorptionStrategy(@Nonnull String ticker, @Nonnull Interval interval, @Nonnull Wallet wallet) {
        final ArrayList<TickerWithInterval> instruments = new ArrayList<>();
        instruments.add(new TickerWithInterval(ticker, interval));
        return new AbsorptionStrategy(instruments, adapter, wallet);
    }
}
