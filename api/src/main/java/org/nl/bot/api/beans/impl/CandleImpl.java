package org.nl.bot.api.beans.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import org.nl.bot.api.Interval;
import org.nl.bot.api.beans.Candle;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonDeserialize(builder = CandleImpl.CandleImplBuilder.class)
@RequiredArgsConstructor
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class CandleImpl implements Candle {

    private final BigDecimal openPrice;

    private final BigDecimal closingPrice;

    private final BigDecimal highestPrice;

    private final BigDecimal lowestPrice;

    private final BigDecimal tradingValue;

    private final ZonedDateTime dateTime;

    private final Interval interval;

    private final String ticker;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CandleImplBuilder {

    }
}
