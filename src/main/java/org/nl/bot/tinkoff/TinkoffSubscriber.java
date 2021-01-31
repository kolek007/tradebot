package org.nl.bot.tinkoff;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.CandleEvent;
import org.nl.bot.api.EventListener;
import org.nl.bot.api.OrderbookEvent;
import org.nl.bot.api.TickerWithInterval;
import org.reactivestreams.example.unicast.AsyncSubscriber;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
public class TinkoffSubscriber extends AsyncSubscriber<StreamingEvent> {
    @Nonnull
    private final BeansConverter converter;
    @Nonnull
    private final Map<InstrumentPerBot, EventListener<CandleEvent>> candleSubscriptions = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<TickerPerBot, EventListener<OrderbookEvent>> orderbookSubscriptions = new ConcurrentHashMap<>();

    public TinkoffSubscriber(@Nonnull final Executor executor, @Nonnull BeansConverter beansConverter) {
        super(executor);
        this.converter = beansConverter;
    }

    public void subscribe(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener) {
        candleSubscriptions.put(new InstrumentPerBot(botId, instrument), listener);
    }

    public void subscribe(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<OrderbookEvent> listener) {
        orderbookSubscriptions.put(new TickerPerBot(botId, ticker), listener);
    }

    @Override
    protected boolean whenNext(final StreamingEvent event) {
//        log.info("Пришло новое событие из Streaming API\n {}", event);
        if(event instanceof StreamingEvent.Candle) {
            final CandleEvent candleEvent = converter.candleEvent((StreamingEvent.Candle) event);
            candleSubscriptions.entrySet().stream().filter(e -> listenerMatches(candleEvent, e)).forEach(e -> {
                e.getValue().onEvent(candleEvent);
            });
        } else if(event instanceof StreamingEvent.Orderbook) {
            final OrderbookEvent orderbookEvent = converter.orderbookEvent((StreamingEvent.Orderbook) event);
            orderbookSubscriptions.entrySet().stream().filter(e -> orderbookEvent.getOrderbook().getTicker().equals(e.getKey().ticker)).forEach(e -> {
                e.getValue().onEvent(orderbookEvent);
            });
        }
        return true;
    }

    private boolean listenerMatches(CandleEvent candleEvent, Map.Entry<InstrumentPerBot, EventListener<CandleEvent>> e) {
        return e.getKey().instrument.getTicker().equals(candleEvent.getCandle().getTicker())
                && e.getKey().instrument.getInterval().equals(candleEvent.getCandle().getInterval());
    }

    public void unsubscribe(@Nonnull String botId, @Nonnull TickerWithInterval instr) {
        candleSubscriptions.remove(new InstrumentPerBot(botId, instr));
    }

    public void unsubscribe(@Nonnull String botId, @Nonnull String ticker) {
        orderbookSubscriptions.remove(new TickerPerBot(botId, ticker));
    }

    public void destroy() {
        candleSubscriptions.keySet().forEach(key -> unsubscribe(key.id, key.instrument));
        orderbookSubscriptions.keySet().forEach(key -> unsubscribe(key.id, key.ticker));
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class InstrumentPerBot {
        @Nonnull
        String id;
        @Nonnull
        TickerWithInterval instrument;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class TickerPerBot {
        @Nonnull
        String id;
        @Nonnull
        String ticker;
    }
}
