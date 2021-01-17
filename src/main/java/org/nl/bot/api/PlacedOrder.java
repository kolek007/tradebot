package org.nl.bot.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public final class PlacedOrder {

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
    public final Status status;

    /**
     * Код причина отказа в размещении.
     */
    @Nullable
    public final String rejectReason;

    /**
     * Причина отказа в размещении (человеческий текст).
     */
    @Nullable
    public final String message;

    /**
     * Желаемое количество лотов.
     */
    public final int requestedLots;

    /**
     * Реально исполненное количество лотов.
     */
    public final int executedLots;

    /**
     * Размер коммиссии.
     */
    @Nullable
    public final MoneyAmount commission;

}
