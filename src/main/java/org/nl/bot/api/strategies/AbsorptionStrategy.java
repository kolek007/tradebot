package org.nl.bot.api.strategies;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AbsorptionStrategy extends AbstractStrategy {

    public AbsorptionStrategy(@Nonnull TickerWithInterval instrument, @Nonnull BrokerAdapter adapter, @Nonnull Wallet wallet) {
        super(Lists.newArrayList(instrument), adapter, wallet);
    }

    @Nonnull
    @Override
    public String getId() {
        return "Absorption " + instruments() + " " + UUID.randomUUID();
    }

    @Override
    public void run() {
        adapter.subscribeCandle(getId(), instruments.get(0), candleEvent -> {
            log.info("Bot {} received candle event {}", getId(), candleEvent);

        });
    }
}
