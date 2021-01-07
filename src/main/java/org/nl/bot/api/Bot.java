package org.nl.bot.api;

import lombok.*;

import javax.annotation.Nonnull;
import java.util.List;

@Builder
@Getter
@EqualsAndHashCode(exclude = "strategy")
@ToString
public class Bot {
    @Nonnull
    private final String name;
    @Nonnull
    @Singular(value = "ticker")
    private final List<String> tickers;

    private final Interval interval;
    @Nonnull
    private final Strategy strategy;
}
