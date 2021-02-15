package org.nl.bot.api.beans.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nl.bot.api.Operation;
import org.nl.bot.api.beans.Order;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class OrderImpl implements Order {
    int lots;
    @Nonnull
    Operation operation;
    @Nonnull
    BigDecimal price;
}
