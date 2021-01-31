package org.nl.bot.api;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public interface Wallet {
    @Nonnull
    BigDecimal getAmount();
}
