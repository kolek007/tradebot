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

    /**
     * Идентификатор заявки.
     */
    @Nonnull
    String getId();

    /**
     * Текущий статус.
     */
    @Nonnull
    Status getStatus();

    /**
     * Код причина отказа в размещении.
     */
    @Nullable
    String getRejectReason();

    /**
     * Причина отказа в размещении (человеческий текст).
     */
    @Nullable
    String getMessage();

    /**
     * Реально исполненное количество лотов.
     */
    int getExecutedLots();

    /**
     * Размер коммиссии.
     */
    @Nullable
    MoneyAmount getCommission();

}
