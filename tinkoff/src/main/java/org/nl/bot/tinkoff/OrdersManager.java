package org.nl.bot.tinkoff;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.EventListener;
import org.nl.bot.api.OrderUpdateEvent;
import org.nl.bot.api.Status;
import org.nl.bot.api.beans.PlacedOrder;
import org.nl.bot.api.subscribers.OrdersSubscriber;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.models.orders.Order;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class OrdersManager implements OrdersSubscriber {
    @Nonnull
    private final BeansConverter converter;
    @Nonnull
    private final OpenApi api;
    @Nonnull
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    @Nonnull
    private final Map<String, EventListener<OrderUpdateEvent>> botSubscriptions = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<String, BotAndPrice> orders = new ConcurrentHashMap<>();

    public void registerOrder(@Nonnull String botId, @Nonnull PlacedOrder order) {
        orders.put(order.getId(), new BotAndPrice(botId, order.getPrice()));
    }

    public void cancelOrder(@Nonnull String orderId) {
        orders.remove(orderId);
    }

    @Override
    public void subscribeOnOrdersUpdate(@Nonnull String botId, @Nonnull EventListener<OrderUpdateEvent> listener) {
        botSubscriptions.put(botId, listener);
    }

    @Override
    public void unsubscribeFromOrdersUpdate(@Nonnull String botId) {
        botSubscriptions.remove(botId);
    }

    public void init() {
        executor.schedule(() -> {
            final List<Order> currentOrders = api.getOrdersContext().getOrders(null).join();
            currentOrders.forEach(o -> {
                final String orderId = o.id;
                final BotAndPrice botAndPrice = orders.get(orderId);
                String botId = botAndPrice.botId;
                final EventListener<OrderUpdateEvent> listener = botSubscriptions.get(botId);
                if (listener != null) {
                    final PlacedOrder order = converter.order(o, botAndPrice.price);
                    if(order.getStatus() == Status.Cancelled || order.getStatus() == Status.Rejected) {
                        orders.remove(orderId);
                    }
                    listener.onEvent(new OrderUpdateEvent(order));
                }
            });
        }, 100, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        botSubscriptions.keySet().forEach(this::unsubscribeFromOrdersUpdate);
    }

    @RequiredArgsConstructor
    private static class BotAndPrice {
        @Nonnull
        String botId;
        @Nonnull
        BigDecimal price;
    }
}
