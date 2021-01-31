package org.nl.bot.tinkoff;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

@Slf4j
public class Wallet implements org.nl.bot.api.Wallet {
    @Nonnull
    @Override
    public BigDecimal getAmount() {
        return null;
    }
}
