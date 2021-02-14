package org.nl.bot.api.subscribers;

import org.nl.bot.api.EventListener;
import org.nl.bot.api.OrderUpdateEvent;

import javax.annotation.Nonnull;

public interface OrdersSubscriber {
    void subscribeOnOrdersUpdate(@Nonnull String botId, @Nonnull EventListener<OrderUpdateEvent> listener);

    void unsubscribeFromOrdersUpdate(@Nonnull String botId);
}
