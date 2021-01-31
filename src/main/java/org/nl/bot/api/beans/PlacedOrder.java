package org.nl.bot.api.beans;

import lombok.*;
import org.nl.bot.api.MoneyAmount;
import org.nl.bot.api.Operation;
import org.nl.bot.api.Status;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PlacedOrder {

    @Nonnull
    String getTicker();

    /**
     * Идентификатор заявки.
     */
    @Nonnull
    String getId();

    /**
     * Тип операции.
     */
    @Nonnull
    Operation getOperation();

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
     * Желаемое количество лотов.
     */
    int getRequestedLots();

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
