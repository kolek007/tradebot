package org.nl.bot.api.strategies;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.api.Strategy;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractStrategy implements Strategy {

    @Nonnull
    protected final List<TickerWithInterval> instruments;
    @Nonnull
    protected final BrokerAdapter adapter;
    @Nonnull
    protected final Map<String, PlacedOrder> placedOrderMap = new HashMap<>();

    @Nonnull
    @Override
    public abstract String getId();

    @Override
    public abstract void run();

    @Override
    public void stop() throws Exception {
        final String id = getId();
        instruments.forEach(instr -> adapter.unsubscribeCandle(id, instr));
        for(String orderId : placedOrderMap.keySet()) {
            adapter.cancelOrder(id, orderId, null);
        }
    }
}
