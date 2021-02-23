package org.nl.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.strategies.AdaptiveStopLossStrategy;
import org.nl.bot.api.strategies.AdaptiveStrategyConfig;
import org.nl.bot.api.strategies.util.trend.PolyTrendLine;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class TradeAdaptiveStopLossTest {
    @Nonnull
    private final BrokerMock broker = new BrokerMock();
    @Nonnull
    private final TickerWithInterval ticker = new TickerWithInterval("BA", Interval.MIN_1);
    private final Wallet wallet =  WalletImpl.builder()
            .amount(new BigDecimal("100.0"))
            .initialAmount(new BigDecimal("100.0"))
            .build();


    @AfterEach
    public void cleanup() {
        broker.cleanUp();
    }

    @Test
    @DisplayName("Historical run against YNDX")
    public void testGrow() {
        List<Candle> read = HistoryUtils.read("C:/trade_data/BA.json");
        AdaptiveStrategyConfig config = AdaptiveStrategyConfig.builder()
                .instrument(ticker)
                .stopLoss(new BigDecimal("0.02"))
                .takeProfit(new BigDecimal("0.01"))
                .minPredictionSize(30)
                .build();
        AdaptiveStopLossStrategy strategy = new AdaptiveStopLossStrategy(config, broker, wallet);
        strategy.run();

        for (Candle candle : read) {
            broker.sendCandle(CandleEvent.builder().candle(candle).build());
        }

        BrokerAssert.assertThat(broker).enteredAt("90.0").exitedAt("95.0");
    }

    @Test
    @DisplayName("Prediction")
    public void testPrediction() {
        PolyTrendLine tl = new PolyTrendLine(1);
        double[] y = {5, 7, 9, 11, 13, 15, 13, 12, 11, 9, 7, 5};
        double[] x = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        tl.setValues(y, x);



        log.info("tl.predict(20) = " + tl.predict(32));

        for (int i = 0; i < 10; i++) {

        }

        System.out.println(Arrays.toString(tl.getCoefficients()));
    }
}
