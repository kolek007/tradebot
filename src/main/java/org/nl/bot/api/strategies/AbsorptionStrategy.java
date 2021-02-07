package org.nl.bot.api.strategies;

import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class AbsorptionStrategy extends AbstractStrategy {

    public AbsorptionStrategy(List<TickerWithInterval> instruments, BrokerAdapter adapter, Wallet wallet) {
        super(instruments, adapter, wallet);
    }

    @Nonnull
    @Override
    public String getId() {
        return "Absorption " + instruments() + " " + UUID.randomUUID();
    }

    public String instruments() {
        StringBuilder builder = new StringBuilder();
        this.instruments.forEach(builder::append);
        return builder.toString();
    }

    @Override
    public void run() {
        adapter.subscribeCandle(getId(), instruments.get(0), candleEvent -> {

        });
    }
}
