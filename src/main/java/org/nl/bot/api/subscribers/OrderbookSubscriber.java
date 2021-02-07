package org.nl.bot.api.subscribers;

import org.nl.bot.api.EventListener;
import org.nl.bot.api.OrderbookEvent;

import javax.annotation.Nonnull;

public interface OrderbookSubscriber {
    void subscribeOrderbook(@Nonnull String botId, @Nonnull String ticker, @Nonnull EventListener<OrderbookEvent> listener);

    void unsubscribeOrderbook(@Nonnull String botId, @Nonnull String ticker);
}
