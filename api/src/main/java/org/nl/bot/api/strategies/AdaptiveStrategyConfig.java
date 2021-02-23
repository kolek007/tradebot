package org.nl.bot.api.strategies;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.nl.bot.api.TickerWithInterval;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Duration;

@Builder
@Getter
@ToString
@Accessors(fluent = true)
public class AdaptiveStrategyConfig {
    @Nonnull
    private final BigDecimal stopLoss;
    @Nonnull
    private final BigDecimal takeProfit;
    @Nonnull
    private final TickerWithInterval instrument;
    @Builder.Default
    private final long timeOffset = Duration.ofMinutes(30).toMillis();
    @Builder.Default
    private final int minPredictionSize = 3;
}
