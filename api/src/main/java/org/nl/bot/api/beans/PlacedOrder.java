package org.nl.bot.api.beans;

import org.nl.bot.api.MoneyAmount;
import org.nl.bot.api.Operation;
import org.nl.bot.api.Status;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

public interface PlacedOrder extends Order {

    @Nonnull
    String getTicker();

    @Nonnull
    String getId();

    @Nonnull
    Status getStatus();

    @Nullable
    String getRejectReason();

    @Nullable
    String getMessage();

    double getExecutedLots();

    @Nullable
    MoneyAmount getCommission();

}
