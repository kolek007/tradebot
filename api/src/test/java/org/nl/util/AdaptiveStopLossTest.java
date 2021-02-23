package org.nl.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.impl.CandleImpl;
import org.nl.bot.api.strategies.AdaptiveStopLossStrategy;
import org.nl.bot.api.strategies.AdaptiveStrategyConfig;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


@Log4j2
public class AdaptiveStopLossTest {
    @Nonnull
    private final BrokerMock broker = new BrokerMock();
    @Nonnull
    private final TickerWithInterval ticker = new TickerWithInterval("test", Interval.MIN_1);
    private final Wallet wallet =  WalletImpl.builder()
            .amount(new BigDecimal("100.0"))
            .initialAmount(new BigDecimal("100.0"))
            .build();
    private final AdaptiveStrategyConfig config = AdaptiveStrategyConfig.builder()
            .instrument(ticker)
            .stopLoss(new BigDecimal("0.02"))
            .takeProfit(new BigDecimal("0.01"))
            .build();

    private long timeStamp = System.currentTimeMillis();


    @AfterEach
    public void cleanup() {
        broker.cleanUp();
    }

    @Test
    @DisplayName("When price go up first and down more then threshold. Then exit as stop loss")
    public void testStopLoss() {
        AdaptiveStopLossStrategy strategy = new AdaptiveStopLossStrategy(config, broker, wallet);
        strategy.run();

        broker.sendCandle(closedAt("90.0"));
        broker.sendCandle(closedAt("95.0"));
        broker.sendCandle(closedAt("100.0"));
        broker.sendCandle(closedAt("100.9"));
        broker.sendCandle(closedAt("99.8"));
        broker.sendCandle(closedAt("99.0"));
        broker.sendCandle(closedAt("95.0"));

        BrokerAssert.assertThat(broker).enteredAt("100.0").exitedAt("95.0");
    }

    @Test
    @DisplayName("When take profit reached. Then exit as stop loss")
    public void testTakeProfit() {
        AdaptiveStopLossStrategy strategy = new AdaptiveStopLossStrategy(config, broker, wallet);
        strategy.run();

        broker.sendCandle(closedAt("90.0"));
        broker.sendCandle(closedAt("95.0"));
        broker.sendCandle(closedAt("100.0"));
        broker.sendCandle(closedAt("100.9"));
        broker.sendCandle(closedAt("99.8"));
        broker.sendCandle(closedAt("99.0"));
        broker.sendCandle(closedAt("101.0"));

        BrokerAssert.assertThat(broker).enteredAt("100.0").exitedAt("101.0");
    }

    @Test
    @DisplayName("When price goes down immediately and hits threshold. Then exit")
    public void testGoesDown() {
        AdaptiveStopLossStrategy strategy = new AdaptiveStopLossStrategy(config, broker, wallet);
        strategy.run();

        broker.sendCandle(closedAt("100.0"));
        broker.sendCandle(closedAt("99.98"));
        broker.sendCandle(closedAt("99.1"));
        broker.sendCandle(closedAt("99.0"));
        broker.sendCandle(closedAt("98.0"));
        broker.sendCandle(closedAt("97.0"));

        BrokerAssert.assertThat(broker).notEntered();
    }

    private CandleEvent closedAt(@Nonnull String closing) {
        return closedAt(new BigDecimal(closing));
    }

    private CandleEvent closedAt(@Nonnull BigDecimal closing) {
        timeStamp = timeStamp + 60 * 1000;
        return CandleEvent.builder()
                .candle(CandleImpl.builder()
                        .ticker(ticker.getTicker())
                        .interval(ticker.getInterval())
                        .closingPrice(closing)
                        .dateTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeStamp),
                                ZoneId.systemDefault()))
                        .openPrice(new BigDecimal("90.0"))
                        .build())
                .build();
    }
}
