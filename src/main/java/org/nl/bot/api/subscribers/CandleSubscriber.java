package org.nl.bot.api.subscribers;

import org.nl.bot.api.CandleEvent;
import org.nl.bot.api.EventListener;
import org.nl.bot.api.TickerWithInterval;

import javax.annotation.Nonnull;

public interface CandleSubscriber {

    void subscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instrument, @Nonnull EventListener<CandleEvent> listener);

    void unsubscribeCandle(@Nonnull String botId, @Nonnull TickerWithInterval instr);


}
