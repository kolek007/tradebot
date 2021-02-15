package org.nl.bot.api.beans;

import org.nl.bot.api.Interval;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface Candle {

        BigDecimal getOpenPrice();

        BigDecimal getClosingPrice();

        BigDecimal getHighestPrice();

        BigDecimal getLowestPrice();

        BigDecimal getTradingValue();

        ZonedDateTime getDateTime();

        Interval getInterval();

        String getTicker();
}
