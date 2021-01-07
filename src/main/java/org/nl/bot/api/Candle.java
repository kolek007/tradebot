package org.nl.bot.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class Candle {
        /**
         * Цена открытия.
         */
        private final BigDecimal openPrice;

        /**
         * Цена закрытия.
         */
        private final BigDecimal closingPrice;

        /**
         * Цена макисмальная цена.
         */
        private final BigDecimal highestPrice;

        /**
         * Минимальная цена.
         */
        private final BigDecimal lowestPrice;

        /**
         * Объём торгов.
         */
        private final BigDecimal tradingValue;

        /**
         * Дата/время формирования свечи.
         */
        private final ZonedDateTime dateTime;

        /**
         * Временной интервал свечи.
         */
        private final Interval interval;

        /**
         * Идентификатор инструмента.
         */
        private final String ticker;
}
