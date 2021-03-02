package org.nl.bot.binance.beans;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.NewOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.nl.bot.api.MoneyAmount;
import org.nl.bot.api.Operation;
import org.nl.bot.api.Status;
import org.nl.bot.api.beans.PlacedOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

@RequiredArgsConstructor
@ToString
public class PlacedOrderImpl implements PlacedOrder {
    @Nonnull
    private final NewOrderResponse newOrderResponse;

    @Override
    public double getLots() {
        return Double.parseDouble(newOrderResponse.getOrigQty());
    }

    @Nonnull
    @Override
    public Operation getOperation() {
        OrderSide side = newOrderResponse.getSide();
        return side == OrderSide.BUY ? Operation.Buy : Operation.Sell;
    }

    @Nonnull
    @Override
    public BigDecimal getPrice() {
        return new BigDecimal(newOrderResponse.getPrice());
    }

    @Nonnull
    @Override
    public String getTicker() {
        return newOrderResponse.getSymbol();
    }

    @Nonnull
    @Override
    public String getId() {
        return String.valueOf(newOrderResponse.getOrderId());
    }

    @Nonnull
    @Override
    public Status getStatus() {
        OrderStatus orderStatus = newOrderResponse.getStatus();
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
        return null;
    }

    @Nullable
    @Override
    public String getMessage() {
        return getRejectReason();
    }

    @Override
    public double getExecutedLots() {
        return Double.parseDouble(newOrderResponse.getCummulativeQuoteQty());
    }

    @Nullable
    @Override
    public MoneyAmount getCommission() {
        return null;
    }
}
