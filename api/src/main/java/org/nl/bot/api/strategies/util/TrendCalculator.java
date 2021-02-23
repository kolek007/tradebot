package org.nl.bot.api.strategies.util;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.nl.bot.api.strategies.util.trend.PolyTrendLine;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Duration;

@Log4j2
@Builder
@Getter
public class TrendCalculator {
    @Builder.Default
    private final long timeOffset = Duration.ofMinutes(30).toMillis();
    @Builder.Default
    private final int minPredictionSize = 3;
    @Builder.Default
    private double[] prices = new double[0];
    @Builder.Default
    private double[] times = new double[0];
    @Nonnull
    private final PolyTrendLine trendLine = new PolyTrendLine(1);

    public Trend calculate(@Nonnull BigDecimal val, long timeStamp) {
        adjust(val, timeStamp);

        if (prices.length < minPredictionSize) {
            return Trend.UNKNOWN;
        }

        trendLine.setValues(prices, times);
        double predictFuture = trendLine.predict(timeStamp + timeOffset);
        double predictPast = trendLine.predict(timeStamp - timeOffset);

        if (predictFuture > predictPast) {
            return Trend.UP;
        }

        if (predictFuture < predictPast) {
            return Trend.DOWN;
        }

        return Trend.NONE;
    }

    public void adjust(@Nonnull BigDecimal val, long timeStamp) {
        long start = timeStamp - timeOffset;

        int position = 0;

        for (int i = 0; i < times.length; i++) {
            if (start<times[i]) {
                position = i;

                break;
            }
        }

        double[] newPrices = new double[prices.length - position + 1];
        double[] newTimes = new double[times.length - position + 1];

        for (int i = position; i < prices.length; i++) {
            newPrices[i-position] = prices[i];
            newTimes[i-position] = times[i];
        }

        newPrices[newPrices.length-1] = val.doubleValue();
        newTimes[newTimes.length-1] = timeStamp;

        prices = newPrices;
        times = newTimes;
    }

    public void reset(@Nonnull double[] prices, @Nonnull double[] times) {
        this.prices = prices;
        this.times = times;
    }
}
