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
    private final Map<String, String> orders2bot = new ConcurrentHashMap<>();

    public void registerOrder(@Nonnull String botId, @Nonnull String orderId) {
        orders2bot.put(orderId, botId);
    }

    public void cancelOrder(@Nonnull String orderId) {
        orders2bot.remove(orderId);
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
                final String botId = orders2bot.get(orderId);
                if(botId != null) {
                    final EventListener<OrderUpdateEvent> listener = botSubscriptions.get(botId);
                    if (listener != null) {
                        final PlacedOrder order = converter.order(o);
                        if(order.getStatus() == Status.Cancelled || order.getStatus() == Status.Rejected) {
                            orders2bot.remove(orderId);
                        }
                        listener.onEvent(new OrderUpdateEvent(order));
                    }
                }
            });
        }, 100, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        botSubscriptions.keySet().forEach(this::unsubscribeFromOrdersUpdate);
    }
}
