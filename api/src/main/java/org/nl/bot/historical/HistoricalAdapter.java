package org.nl.bot.historical;

import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.Order;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.sandbox.SandboxAdapter;
import org.nl.bot.sandbox.beans.PlacedOrderSbx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public class HistoricalAdapter extends SandboxAdapter {

    private final Executor exec;

    public HistoricalAdapter(@Nonnull BrokerAdapter adapter, @Nonnull Executor executor) {
        super(adapter, executor);
        exec = executor;
    }

    @Override
    public void subscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener) {
        exec.execute(() -> {
            int days = 31 * 24 * 60;
            for (; days > 0; days = days - 1000) {
                CompletableFuture<Optional<List<Candle>>> historicalCandles = getHistoricalCandles(instrument.getTicker(), OffsetDateTime.now().minusMinutes(2 * days), OffsetDateTime.now().minusMinutes(days), instrument.getInterval());
                List<Candle> candles = historicalCandles.join().get();
                candles.forEach(c -> {
                    listener.onEvent(CandleEvent.builder().candle(c).build());
                });
            }
        });
    }

    @Nonnull
    @Override
    public CompletableFuture<PlacedOrder> placeOrder(@Nonnull String botId, @Nonnull String ticker, @Nonnull Order limitOrder, @Nullable String brokerAccountId) {
        CompletableFuture<PlacedOrder> future = new CompletableFuture<>();
        final PlacedOrderSbx placedOrder = PlacedOrderSbx.builder()
                .ticker(ticker)
                .status(Status.New)
                .id(UUID.randomUUID().toString())
                .lots(limitOrder.getLots())
                .operation(limitOrder.getOperation())
                .price(limitOrder.getPrice())
                .build();
        future.complete(placedOrder);
        return future;

    }
}
