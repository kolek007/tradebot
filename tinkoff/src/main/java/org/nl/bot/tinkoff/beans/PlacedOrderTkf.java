package org.nl.bot.tinkoff.beans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
public class PlacedOrderTkf implements PlacedOrder {

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

    @Nonnull
    public BigDecimal price;

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
    public int lots;

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
