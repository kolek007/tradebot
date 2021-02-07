package org.nl.bot.api;

import lombok.Builder;
import lombok.Getter;
import org.nl.bot.api.beans.PlacedOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Builder
public class WalletImpl implements Wallet, EventListener<OrderUpdateEvent> {
    @Nonnull
    private final BrokerAdapter adapter;
    @Nonnull
    @Getter
    private final BigDecimal initialAmount;
    @Nullable
    @Getter
    private final BigDecimal limit;
    @Nonnull
    @Getter
    private BigDecimal amount;

    @Nonnull
    @Getter
    protected final Map<String, PlacedOrder> orders = new ConcurrentHashMap<>();

    public boolean withdraw(@Nonnull BigDecimal value) {
        if(amount.compareTo(value) < 0) {
            return false;
        }
        amount = amount.subtract(value);
        return true;
    }

    public void enroll(@Nonnull BigDecimal value) {
        amount = amount.add(value);
    }

    @Override
    public void onEvent(OrderUpdateEvent event) {
        //todo compute amounts
        orders.put(event.getOrder().getId(), event.getOrder());
    }
}
