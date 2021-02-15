package org.nl.bot.api.beans.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.nl.bot.api.Interval;
import org.nl.bot.api.beans.Candle;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Getter
@Builder
@ToString
public class CandleImpl implements Candle {

    private final BigDecimal openPrice;

    private final BigDecimal closingPrice;

    private final BigDecimal highestPrice;

    private final BigDecimal lowestPrice;

    private final BigDecimal tradingValue;

    private final ZonedDateTime dateTime;

    private final Interval interval;

    private final String ticker;
}
