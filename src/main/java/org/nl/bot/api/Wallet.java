package org.nl.bot.api;

import org.nl.bot.api.beans.PlacedOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;

public interface Wallet {

    @Nonnull
    BigDecimal getInitialAmount();

    @Nullable
    BigDecimal getLimit();

    @Nonnull
    BigDecimal getAmount();

    boolean withdraw(@Nonnull BigDecimal value);

    void enroll(@Nonnull BigDecimal value);

    @Nonnull
    Map<String, PlacedOrder> getOrders();
}
