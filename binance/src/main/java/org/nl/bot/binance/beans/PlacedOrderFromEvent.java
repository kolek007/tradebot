package org.nl.bot.binance.beans;

import com.binance.api.client.domain.OrderRejectReason;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.event.OrderTradeUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.nl.bot.api.Currency;
import org.nl.bot.api.MoneyAmount;
import org.nl.bot.api.Operation;
import org.nl.bot.api.Status;
import org.nl.bot.api.beans.PlacedOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

@RequiredArgsConstructor
@ToString
public class PlacedOrderFromEvent implements PlacedOrder {
    @Nonnull
    private final OrderTradeUpdateEvent orderTradeUpdateEvent;

    @Override
    public double getLots() {
        return Double.parseDouble(orderTradeUpdateEvent.getOriginalQuantity());
    }

    @Nonnull
    @Override
    public Operation getOperation() {
        OrderSide side = orderTradeUpdateEvent.getSide();
        return side == OrderSide.BUY ? Operation.Buy : Operation.Sell;
    }

    @Nonnull
    @Override
    public BigDecimal getPrice() {
        return new BigDecimal(orderTradeUpdateEvent.getPrice());
    }

    @Nonnull
    @Override
    public String getTicker() {
        return orderTradeUpdateEvent.getSymbol();
    }

    @Nonnull
    @Override
    public String getId() {
        return String.valueOf(orderTradeUpdateEvent.getOrderId());
    }

    @Nonnull
    @Override
    public Status getStatus() {
        OrderStatus orderStatus = orderTradeUpdateEvent.getOrderStatus();
        switch (orderStatus) {
            case NEW:
            case PARTIALLY_FILLED:
            case FILLED:
                return Status.New;
            case CANCELED:
            case PENDING_CANCEL:
                return Status.Cancelled;
            case REJECTED:
            case EXPIRED:
                return Status.Rejected;
        }
        throw new RuntimeException("Couldn't recognize status " + orderStatus);
    }

    @Nullable
    @Override
    public String getRejectReason() {
        OrderRejectReason orderRejectReason = orderTradeUpdateEvent.getOrderRejectReason();
        if(orderRejectReason != null) {
            return orderRejectReason.toString();
        }
        return null;
    }

    @Nullable
    @Override
    public String getMessage() {
        return getRejectReason();
    }

    @Override
    public double getExecutedLots() {
        return Double.parseDouble(orderTradeUpdateEvent.getAccumulatedQuantity());
    }

    @Nullable
    @Override
    public MoneyAmount getCommission() {
        return new MoneyAmount(Currency.UNKNOWN, new BigDecimal(orderTradeUpdateEvent.getCommission()));
    }
}
