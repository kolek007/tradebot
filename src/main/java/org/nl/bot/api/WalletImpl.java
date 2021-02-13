package org.nl.bot.api;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.beans.PlacedOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Builder
public class WalletImpl implements Wallet, EventListener<OrderUpdateEvent> {
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
        log.info("Withdrawal: {}. Current amount is {}", value.toPlainString(), amount.toPlainString());
        if(amount.compareTo(value) < 0) {
            return false;
        }
        amount = amount.subtract(value);
        return true;
    }

    public void enroll(@Nonnull BigDecimal value) {
        log.info("Enrolling: {}. Current amount is {}", value.toPlainString(), amount.toPlainString());
        amount = amount.add(value);
    }

    @Override
    public void onEvent(OrderUpdateEvent event) {
        PlacedOrder order = event.getOrder();
        orders.put(order.getId(), order);
        if(order.getExecutedLots() == order.getLots()) { //Order executed completely
            if (order.getOperation() == Operation.Buy) {
                withdraw(order.getPrice().multiply(BigDecimal.valueOf(order.getLots())));
            } else {
                enroll(order.getPrice().multiply(BigDecimal.valueOf(order.getLots())));
            }
            if (order.getCommission() != null) {
                withdraw(order.getCommission().getValue());
            }
        }
    }
}
