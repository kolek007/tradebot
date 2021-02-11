package org.nl.bot.api.strategies;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;
import org.nl.bot.api.beans.Candle;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class AbsorptionStrategy extends AbstractStrategy {

    @Nonnull
    private final TickerWithInterval instrument;

    public AbsorptionStrategy(@Nonnull TickerWithInterval instrument, @Nonnull BrokerAdapter adapter, @Nonnull Wallet wallet) {
        super(Lists.newArrayList(instrument), adapter, wallet);
        this.instrument = instrument;
    }

    @Nonnull
    @Override
    public String getId() {
        return "Absorption " + instruments() + " " + UUID.randomUUID();
    }

    @Override
    public void run() {
        Optional<List<Candle>> candles = adapter.getHistoricalCandles(instrument.getTicker(), OffsetDateTime.now().minusHours(2), OffsetDateTime.now(), instrument.getInterval()).join();
        candles.ifPresent(list -> list.forEach(c -> log.info("Historical candle {}", c)));
        adapter.subscribeCandle(getId(), instruments.get(0), candleEvent -> {
            log.info("Bot {} received candle event {}", getId(), candleEvent);

        });
    }
}
