package org.nl.bot.api;

import lombok.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public final class PlacedOrder {

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
    @Nonnull
    public Status status;

    /**
     * Код причина отказа в размещении.
     */
    @Nullable
    public String rejectReason;

    /**
     * Причина отказа в размещении (человеческий текст).
     */
    @Nullable
    public String message;

    /**
     * Желаемое количество лотов.
     */
    public int requestedLots;

    /**
     * Реально исполненное количество лотов.
     */
    public int executedLots;

    /**
     * Размер коммиссии.
     */
    @Nullable
    public MoneyAmount commission;

}
