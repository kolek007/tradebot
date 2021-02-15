package org.nl.bot.sandbox.beans;

import lombok.*;
import org.nl.bot.api.MoneyAmount;
import org.nl.bot.api.Operation;
import org.nl.bot.api.Status;
import org.nl.bot.api.beans.PlacedOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class PlacedOrderSbx implements PlacedOrder {

    @Nonnull
    public final String ticker;

    @Nonnull
    public final String id;

    @Nonnull
    public final Operation operation;

    @Setter
    @Nonnull
    public Status status;

    @Nonnull
    public BigDecimal price;

    @Setter
    @Nullable
    public String rejectReason;

    @Setter
    @Nullable
    public String message;

    @Setter
    public int lots;

    @Setter
    public int executedLots;

    @Setter
    @Nullable
    public MoneyAmount commission;
}
