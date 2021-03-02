package org.nl.bot.tinkoff.beans;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.nl.bot.api.Operation;
import org.nl.bot.api.beans.Order;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class OrderTkf implements Order {
    private final double lots;
    @Nonnull
    private final Operation operation;
    @Nonnull
    private final BigDecimal price;
}
