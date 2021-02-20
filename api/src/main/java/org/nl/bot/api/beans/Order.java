package org.nl.bot.api.beans;

import org.nl.bot.api.Operation;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public interface Order {

    double getLots();
    @Nonnull
    Operation getOperation();
    @Nonnull
    BigDecimal getPrice();
}
