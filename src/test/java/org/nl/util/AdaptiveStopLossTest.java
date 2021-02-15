package org.nl.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nl.bot.api.*;
import org.nl.bot.api.strategies.AdaptiveStopLossStrategy;
import org.nl.bot.tinkoff.beans.CandleTkf;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Nonnull;
import java.math.BigDecimal;


@ExtendWith(SpringExtension.class)
public class AdaptiveStopLossTest {
    @Nonnull
    private final BrokerMock broker = new BrokerMock();
    @Nonnull
    private final TickerWithInterval ticker = new TickerWithInterval("test", Interval.MIN_1);
    private final Wallet wallet =  WalletImpl.builder()
            .amount(new BigDecimal("100.0"))
            .initialAmount(new BigDecimal("100.0"))
            .build();


    @AfterEach
    public void cleanup() {
        broker.cleanUp();
    }

    @Test
    @DisplayName("When price go up first and down more then threshold. Then exit")
    public void testGrow() {
        AdaptiveStopLossStrategy strategy = new AdaptiveStopLossStrategy(ticker, broker, wallet, new BigDecimal("0.02"));
        strategy.run();

        broker.sendCandle(closedAt("90.0"));
        broker.sendCandle(closedAt("95.0"));
        broker.sendCandle(closedAt("100.0"));
        broker.sendCandle(closedAt("95.0"));
        broker.sendCandle(closedAt("93.0"));

        BrokerAssert.assertThat(broker).enteredAt("90.0").exitedAt("95.0");
    }

    @Test
    @DisplayName("When price goes down immediately and hits threshold. Then exit")
    public void testGoesDown() {
        AdaptiveStopLossStrategy strategy = new AdaptiveStopLossStrategy(ticker, broker, wallet, new BigDecimal("0.02"));
        strategy.run();

        broker.sendCandle(closedAt("100.0"));
        broker.sendCandle(closedAt("99.98"));
        broker.sendCandle(closedAt("99.1"));
        broker.sendCandle(closedAt("99.0"));
        broker.sendCandle(closedAt("98.0"));
        broker.sendCandle(closedAt("97.0"));

        BrokerAssert.assertThat(broker).enteredAt("100.0").exitedAt("98.0");
    }

    @Test
    @DisplayName("When price goes up/down without hitting threshold. Then no exit ")
    public void testGoesUpDown() {
        AdaptiveStopLossStrategy strategy = new AdaptiveStopLossStrategy(ticker, broker, wallet, new BigDecimal("0.02"));
        strategy.run();

        broker.sendCandle(closedAt("100.0"));
        broker.sendCandle(closedAt("99.98"));
        broker.sendCandle(closedAt("100.1"));
        broker.sendCandle(closedAt("101.0"));
        broker.sendCandle(closedAt("99.9"));
        broker.sendCandle(closedAt("102.0"));
        broker.sendCandle(closedAt("101.0"));
        broker.sendCandle(closedAt("100.5"));

        BrokerAssert.assertThat(broker).enteredAt("100.0").notExited();
    }

    private CandleEvent closedAt(@NotNull String closing) {
        return closedAt(new BigDecimal(closing));
    }

    private CandleEvent closedAt(@NotNull BigDecimal closing) {
        return CandleEvent.builder()
                .candle(CandleTkf.builder()
                        .ticker(ticker.getTicker())
                        .interval(ticker.getInterval())
                        .closingPrice(closing)
                        .openPrice(new BigDecimal("90.0"))
                        .build())
                .build();
    }
}
