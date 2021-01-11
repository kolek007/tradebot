package org.nl.bot.api;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class MoneyAmount {
    @Nonnull
    public final Currency currency;

    @Nonnull
    public final BigDecimal value;
}
