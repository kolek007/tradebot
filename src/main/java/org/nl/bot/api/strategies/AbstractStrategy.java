package org.nl.bot.api.strategies;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Strategy;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractStrategy implements Strategy {

    @Nonnull
    protected final List<TickerWithInterval> instruments;
    @Nonnull
    protected final BrokerAdapter adapter;
    @Nonnull
    @Getter
    protected final Wallet wallet;

    @Nonnull
    @Override
    public abstract String getId();

    @Override
    public abstract void run();

    @Override
    public void stop() throws Exception {
        final String id = getId();
        instruments.forEach(instr -> adapter.unsubscribeCandle(id, instr));
        for(String orderId : getWallet().getOrders().keySet()) {
            adapter.cancelOrder(id, orderId, null);
        }
    }

    @Nonnull
    @Override
    public Wallet wallet() {
        return wallet;
    }
}
