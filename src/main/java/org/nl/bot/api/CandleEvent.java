package org.nl.bot.api;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.nl.bot.api.beans.Candle;

import javax.annotation.Nonnull;

@Builder
@Getter
@ToString
public class CandleEvent {
    @Nonnull
    private final Candle candle;

}
