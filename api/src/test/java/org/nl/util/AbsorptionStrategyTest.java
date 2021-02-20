package org.nl.util;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.impl.CandleImpl;
import org.nl.bot.api.strategies.AbsorptionStrategy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Disabled
public class AbsorptionStrategyTest {
    @Nonnull
    private BrokerMock broker = new BrokerMock();
    @Nonnull
    private final TickerWithInterval ticker = new TickerWithInterval("test", Interval.MIN_1);
    private final Wallet wallet =  WalletImpl.builder()
            .amount(new BigDecimal("1000.0"))
            .initialAmount(new BigDecimal("1000.0"))
            .build();

    @BeforeEach
    public void init() {
        broker = spy(broker);
        List<Candle> list = Lists.newArrayList(candle(20d, 10d, 21d, 9d), candle(20d, 10d, 21d, 9d), candle(20d, 10d, 21d, 9d));
        doReturn(new CompletableFuture<Optional<List<Candle>>>() {
            @Override
            public Optional<List<Candle>> get() throws InterruptedException, ExecutionException {
                return Optional.of(list);
            }

            @Override
            public Optional<List<Candle>> join() {
                return Optional.of(list);
            }
        }).when(broker).getHistoricalCandles(eq("test"), any(), any(), eq(Interval.MIN_1));
    }

    @AfterEach
    public void cleanup() {
        broker.cleanUp();
    }

    @Test
    @DisplayName("When price go up first and down more then threshold. Then exit")
    public void testGrow() {

        AbsorptionStrategy strategy = new AbsorptionStrategy(ticker, broker, wallet);
        strategy.run();

        broker.sendCandle(candleEvent(200d, 100d, 209d, 91d));
        broker.sendCandle(candleEvent(100d, 200d, 209d, 91d));
        broker.sendCandle(candleEvent(200d, 300d, 310d, 190d));


        BrokerAssert.assertThat(broker).enteredAt("200.0").exitedAt("220.0");
    }

    private CandleEvent candleEvent(double open, double close, double high, double low) {
        return candleEvent(BigDecimal.valueOf(open), BigDecimal.valueOf(close), BigDecimal.valueOf(high), BigDecimal.valueOf(low));
    }

    private CandleEvent candleEvent(@Nonnull BigDecimal opening, @Nonnull BigDecimal closing, @Nonnull BigDecimal high, @Nonnull BigDecimal low) {
        return CandleEvent.builder()
                .candle(CandleImpl.builder()
                        .ticker(ticker.getTicker())
                        .interval(ticker.getInterval())
                        .closingPrice(closing)
                        .openPrice(opening)
                        .highestPrice(high)
                        .lowestPrice(low)
                        .dateTime(ZonedDateTime.of(2000, 1, 11,1,1,1, Math.abs(new Random().nextInt() % 999999999), ZoneId.systemDefault()))
                        .build())
                .build();
    }

    private Candle candle(double open, double close, double high, double low) {
        return candle(BigDecimal.valueOf(open), BigDecimal.valueOf(close), BigDecimal.valueOf(high), BigDecimal.valueOf(low));
    }

    private Candle candle(@Nonnull BigDecimal opening, @Nonnull BigDecimal closing, @Nonnull BigDecimal high, @Nonnull BigDecimal low) {
        return CandleImpl.builder()
                .ticker(ticker.getTicker())
                .interval(ticker.getInterval())
                .closingPrice(closing)
                .openPrice(opening)
                .highestPrice(high)
                .lowestPrice(low)
                .build();
    }
}
