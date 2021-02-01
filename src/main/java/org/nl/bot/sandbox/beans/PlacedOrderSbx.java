package org.nl.bot.sandbox.beans;

import lombok.*;
import org.nl.bot.api.MoneyAmount;
import org.nl.bot.api.Operation;
import org.nl.bot.api.Status;
import org.nl.bot.api.beans.PlacedOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class PlacedOrderSbx implements PlacedOrder {

    @Nonnull
    public final String ticker;

    /**
     * Идентификатор заявки.
     */
    @Nonnull
    public final String id;

    /**
     * Тип операции.
     */
    @Nonnull
    public final Operation operation;

    /**
     * Текущий статус.
     */
    @Setter
    @Nonnull
    public Status status;

    /**
     * Код причина отказа в размещении.
     */
    @Setter
    @Nullable
    public String rejectReason;

    /**
     * Причина отказа в размещении (человеческий текст).
     */
    @Setter
    @Nullable
    public String message;

    /**
     * Желаемое количество лотов.
     */
    @Setter
    public int requestedLots;

    /**
     * Реально исполненное количество лотов.
     */
    @Setter
    public int executedLots;

    /**
     * Размер коммиссии.
     */
    @Setter
    @Nullable
    public MoneyAmount commission;
}