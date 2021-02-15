package org.nl.bot.tinkoff;

import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.*;
import org.nl.bot.api.subscribers.CandleSubscriber;
import org.nl.bot.api.subscribers.OrderbookSubscriber;
import org.reactivestreams.example.unicast.AsyncSubscriber;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
public class TinkoffSubscriber extends AsyncSubscriber<StreamingEvent> implements CandleSubscriber, OrderbookSubscriber {
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

    @Override
    public void subscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener) {
        candleSubscriptions.put(new InstrumentPerBot(botId, instrument), listener);
    }

    @Override
    public void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<OrderbookEvent> listener) {
        orderbookSubscriptions.put(new TickerPerBot(botId, ticker), listener);
    }

    @Override
    protected boolean whenNext(final StreamingEvent event) {
//        log.info("Пришло новое событие из Streaming API\n {}", event);
        if(event instanceof StreamingEvent.Candle) {
            final CandleEvent candleEvent = converter.candleEvent((StreamingEvent.Candle) event);
            candleSubscriptions.entrySet().stream().filter(e -> listenerMatches(candleEvent, e)).forEach(e -> e.getValue().onEvent(candleEvent));
        } else if(event instanceof StreamingEvent.Orderbook) {
            final OrderbookEvent orderbookEvent = converter.orderbookEvent((StreamingEvent.Orderbook) event);
            orderbookSubscriptions.entrySet().stream().filter(e -> orderbookEvent.getOrderbook().getTicker().equals(e.getKey().getTicker())).forEach(e -> e.getValue().onEvent(orderbookEvent));
        }
        return true;
    }

    private boolean listenerMatches(CandleEvent candleEvent, Map.Entry<InstrumentPerBot, EventListener<CandleEvent>> e) {
        return e.getKey().getInstrument().getTicker().equals(candleEvent.getCandle().getTicker())
                && e.getKey().getInstrument().getInterval().equals(candleEvent.getCandle().getInterval());
    }

    @Override
    public void unsubscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instr) {
        candleSubscriptions.remove(new InstrumentPerBot(botId, instr));
    }

    @Override
    public void unsubscribeOrderbook(@Nonnull String botId, @Nonnull String ticker) {
        orderbookSubscriptions.remove(new TickerPerBot(botId, ticker));
    }

    public void destroy() {
        candleSubscriptions.keySet().forEach(key -> unsubscribeCandle(key.getId(), key.getInstrument()));
        orderbookSubscriptions.keySet().forEach(key -> unsubscribeOrderbook(key.getId(), key.getTicker()));
    }

}
