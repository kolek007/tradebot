package org.nl.bot.tinkoff;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.Instrument;
import org.nl.bot.api.TickerEvent;
import org.reactivestreams.Subscriber;
import org.reactivestreams.example.unicast.AsyncSubscriber;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
public class StreamingApiSubscriber extends AsyncSubscriber<StreamingEvent> {
    @Nonnull
    private final BeansConverter converter;
    @Nonnull
    private final Map<InstrumentPerBot, Subscriber<TickerEvent>> subscriptions = new ConcurrentHashMap<>();

    public StreamingApiSubscriber(@Nonnull final Executor executor, @Nonnull BeansConverter beansConverter) {
        super(executor);
        this.converter = beansConverter;
    }

    public void subscribe(@Nonnull String botId, @Nonnull Instrument instrument, @Nonnull Subscriber<TickerEvent> listener) {
        subscriptions.put(new InstrumentPerBot(botId, instrument), listener);
    }

    @Override
    protected boolean whenNext(final StreamingEvent event) {
        log.info("Пришло новое событие из Streaming API\n {}", event);
        if(event instanceof StreamingEvent.Candle) {
            final TickerEvent tickerEvent = converter.candleEvent((StreamingEvent.Candle) event);
            subscriptions.entrySet().stream().filter(e -> listenerMatches(tickerEvent, e)).forEach(e -> {
                e.getValue().onNext(tickerEvent);
            });
        }
        return true;
    }

    private boolean listenerMatches(TickerEvent tickerEvent, Map.Entry<InstrumentPerBot, Subscriber<TickerEvent>> e) {
        return e.getKey().instrument.getTicker().equals(tickerEvent.getCandle().getTicker())
                && e.getKey().instrument.getInterval().equals(tickerEvent.getCandle().getInterval());
    }

    public void unsubscribe(@Nonnull String botId, @Nonnull Instrument instr) {
        subscriptions.remove(new InstrumentPerBot(botId, instr));
    }

    public void destroy() {
        subscriptions.keySet().forEach(key -> unsubscribe(key.id, key.instrument));
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class InstrumentPerBot {
        @Nonnull
        String id;
        @Nonnull
        Instrument instrument;
    }
}
