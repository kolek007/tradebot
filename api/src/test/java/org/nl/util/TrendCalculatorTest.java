package org.nl.util;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nl.bot.api.strategies.util.Trend;
import org.nl.bot.api.strategies.util.TrendCalculator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Log4j2
public class TrendCalculatorTest {
    @Test
    @DisplayName("Test empty prices/times - when no history is available")
    public void testEmpty() {
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[0])
                .times(new double[0])
                .build();

        long timeStamp = System.currentTimeMillis();
        calc.adjust(new BigDecimal("103"), timeStamp);

        Assertions.assertThat(calc.getPrices()).containsExactly(103);
        Assertions.assertThat(calc.getTimes()).containsExactly(timeStamp);
    }

    @Test
    @DisplayName("When data is insufficient for trend calculation")
    public void testTrendUnknown() {
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .minPredictionSize(4)
                .prices(new double[]{100, 105, 110})
                .times(new double[]{
                        OffsetDateTime.now().minusHours(1).toEpochSecond() * 1000,
                        minus10,
                        minus5
                })
                .build();

        long timeStamp = System.currentTimeMillis();
        Trend calculate = calc.calculate(new BigDecimal("103"), timeStamp);

        Assertions.assertThat(calculate).isEqualTo(Trend.UNKNOWN);
    }

    @Test
    @DisplayName("When first element is obsolete by time offset it removed and new Value added")
    public void testObsoleteTime() {
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[]{100, 105, 110})
                .times(new double[]{
                        OffsetDateTime.now().minusHours(1).toEpochSecond() * 1000,
                        minus10,
                        minus5
                })
                .build();

        long timeStamp = System.currentTimeMillis();
        calc.calculate(new BigDecimal("103"), timeStamp);

        Assertions.assertThat(calc.getPrices()).containsExactly(105,110,103);
        Assertions.assertThat(calc.getTimes()).containsExactly(minus10,minus5,timeStamp);
    }

    @Test
    @DisplayName("When no obsolete values historical data just increased with new price")
    public void testAllTimesArOk() {
        long minus15 = OffsetDateTime.now().minusMinutes(15).toEpochSecond() * 1000;
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[]{100, 105, 110})
                .times(new double[]{
                        minus15,
                        minus10,
                        minus5
                })
                .build();

        long timeStamp = System.currentTimeMillis();
        calc.calculate(new BigDecimal("103"), timeStamp);

        Assertions.assertThat(calc.getPrices()).containsExactly(100,105,110,103);
        Assertions.assertThat(calc.getTimes()).containsExactly(minus15, minus10,minus5,timeStamp);
    }


    @Test
    @DisplayName("When price goes up monotonically")
    public void testTrendIsUp() {
        long minus15 = OffsetDateTime.now().minusMinutes(15).toEpochSecond() * 1000;
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        long minus2 = OffsetDateTime.now().minusMinutes(2).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[]{100, 105, 110, 111})
                .times(new double[]{
                        minus15,
                        minus10,
                        minus5,
                        minus2,

                })
                .build();

        long timeStamp = System.currentTimeMillis();

        Assertions.assertThat(Trend.UP).isEqualTo(calc.calculate(new BigDecimal("112"), timeStamp));
    }

    @Test
    @DisplayName("When price goes down monotonically")
    public void testTrendIsDown() {
        long minus15 = OffsetDateTime.now().minusMinutes(15).toEpochSecond() * 1000;
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        long minus2 = OffsetDateTime.now().minusMinutes(2).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[]{110, 105, 103, 101})
                .times(new double[]{
                        minus15,
                        minus10,
                        minus5,
                        minus2,

                })
                .build();

        long timeStamp = System.currentTimeMillis();

        Assertions.assertThat(Trend.DOWN).isEqualTo(calc.calculate(new BigDecimal("100"), timeStamp));
    }

    @Test
    @DisplayName("When price goes up and down but in general trend is to go UP")
    public void testTrendIsUoNonMono() {
        long minus15 = OffsetDateTime.now().minusMinutes(15).toEpochSecond() * 1000;
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        long minus4 = OffsetDateTime.now().minusMinutes(4).toEpochSecond() * 1000;
        long minus3 = OffsetDateTime.now().minusMinutes(3).toEpochSecond() * 1000;
        long minus2 = OffsetDateTime.now().minusMinutes(2).toEpochSecond() * 1000;
        long minus1 = OffsetDateTime.now().minusMinutes(1).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[]{108, 105, 106,107,106,108, 107.5})
                .times(new double[]{
                        minus15,
                        minus10,
                        minus5,
                        minus4,
                        minus3,
                        minus2,
                        minus1,

                })
                .build();

        long timeStamp = System.currentTimeMillis();

        Assertions.assertThat(Trend.UP).isEqualTo(calc.calculate(new BigDecimal("107"), timeStamp));
    }

    @Test
    @DisplayName("checkDoubleCup first case")
    public void testDoubleCup() {
        log.debug("Yooo");
        long minus15 = OffsetDateTime.now().minusMinutes(15).toEpochSecond() * 1000;
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus9 = OffsetDateTime.now().minusMinutes(9).toEpochSecond() * 1000;
        long minus8 = OffsetDateTime.now().minusMinutes(8).toEpochSecond() * 1000;
        long minus7 = OffsetDateTime.now().minusMinutes(7).toEpochSecond() * 1000;
        long minus6 = OffsetDateTime.now().minusMinutes(6).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        long minus4 = OffsetDateTime.now().minusMinutes(4).toEpochSecond() * 1000;
        long minus3 = OffsetDateTime.now().minusMinutes(3).toEpochSecond() * 1000;
        long minus2 = OffsetDateTime.now().minusMinutes(2).toEpochSecond() * 1000;
        long minus1 = OffsetDateTime.now().minusMinutes(1).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[]{110, 109, 108,106,107,108, 110, 109, 108, 109})
                .times(new double[]{
                        minus10,
                        minus9,
                        minus8,
                        minus7,
                        minus6,
                        minus5,
                        minus4,
                        minus3,
                        minus2,
                        minus1,

                })
                .build();

        long timeStamp = System.currentTimeMillis();

        Assertions.assertThat(Trend.UP).isEqualTo(calc.calculate(new BigDecimal("110.5"), timeStamp));
    }

    @Test
    @DisplayName("checkDoubleCup second case")
    public void testDoubleCup2() {
        long minus15 = OffsetDateTime.now().minusMinutes(15).toEpochSecond() * 1000;
        long minus10 = OffsetDateTime.now().minusMinutes(10).toEpochSecond() * 1000;
        long minus9 = OffsetDateTime.now().minusMinutes(9).toEpochSecond() * 1000;
        long minus8 = OffsetDateTime.now().minusMinutes(8).toEpochSecond() * 1000;
        long minus7 = OffsetDateTime.now().minusMinutes(7).toEpochSecond() * 1000;
        long minus6 = OffsetDateTime.now().minusMinutes(6).toEpochSecond() * 1000;
        long minus5 = OffsetDateTime.now().minusMinutes(5).toEpochSecond() * 1000;
        long minus4 = OffsetDateTime.now().minusMinutes(4).toEpochSecond() * 1000;
        long minus3 = OffsetDateTime.now().minusMinutes(3).toEpochSecond() * 1000;
        long minus2 = OffsetDateTime.now().minusMinutes(2).toEpochSecond() * 1000;
        long minus1 = OffsetDateTime.now().minusMinutes(1).toEpochSecond() * 1000;
        TrendCalculator calc = TrendCalculator.builder()
                .prices(new double[]{110, 109, 108,106,107,108, 110, 108, 106, 104})
                .times(new double[]{
                        minus10,
                        minus9,
                        minus8,
                        minus7,
                        minus6,
                        minus5,
                        minus4,
                        minus3,
                        minus2,
                        minus1,

                })
                .build();

        long timeStamp = System.currentTimeMillis();

        Assertions.assertThat(Trend.DOWN).isEqualTo(calc.calculate(new BigDecimal("110"), timeStamp));
    }
}
