package org.nl.bot.api.beans;

import org.nl.bot.api.Interval;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface Candle {
        /**
         * Цена открытия.
         */
        BigDecimal getOpenPrice();

        /**
         * Цена закрытия.
         */
        BigDecimal getClosingPrice();

        /**
         * Цена макисмальная цена.
         */
        BigDecimal getHighestPrice();

        /**
         * Минимальная цена.
         */
        BigDecimal getLowestPrice();

        /**
         * Объём торгов.
         */
        BigDecimal getTradingValue();

        /**
         * Дата/время формирования свечи.
         */
        ZonedDateTime getDateTime();

        /**
         * Временной интервал свечи.
         */
        Interval getInterval();

        /**
         * Идентификатор инструмента.
         */
        String getTicker();
}
