package org.nl.bot.api.strategies;

import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Interval;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;
import org.nl.bot.api.annotations.Strategy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StrategiesFactory {

    @Strategy(name = "Absorption")
    public AbsorptionStrategy createAbsorptionStrategy(@Nonnull String[] ticker, @Nonnull Interval[] interval, @Nonnull Wallet wallet, @Nonnull BrokerAdapter adapter) {
        return new AbsorptionStrategy(new TickerWithInterval(ticker[0], interval[0]), adapter, wallet);
    }

    public org.nl.bot.api.Strategy createStrategy(
            @Nonnull String name,
            @Nonnull String[] tickers,
            @Nonnull Interval[] intervals,
            @Nonnull Wallet wallet,
            @Nonnull BrokerAdapter adapter) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            Strategy annotation = method.getAnnotation(Strategy.class);
            if(annotation != null && annotation.name().equals(name)) {
                return (org.nl.bot.api.Strategy) method.invoke(this, tickers, intervals, wallet, adapter);
            }
        }
        throw new RuntimeException("Strategy with name '" + name + "' wasn't found");
    }

    @Nonnull
    @Strategy(name = "AdaptiveStopLossStrategy")
    public AdaptiveStopLossStrategy createAdaptiveStopLossStrategy(@Nonnull String[] ticker, @Nonnull Interval[] interval, @Nonnull Wallet wallet, @Nonnull BrokerAdapter adapter) {
        AdaptiveStrategyConfig config = AdaptiveStrategyConfig.builder()
                .minPredictionSize(30)
                .stopLoss(new BigDecimal("0.02"))
                .takeProfit(new BigDecimal("0.02"))
                .instrument(new TickerWithInterval(ticker[0], interval[0]))
                .build();

        return new AdaptiveStopLossStrategy(config, adapter, wallet);
    }

    @Nonnull
    @Strategy(name = "SlidingMinMaxStrategy")
    public SlidingMinMaxStrategy createSlidingMinMaxStrategy(@Nonnull String[] ticker, @Nonnull Interval[] interval, @Nonnull Wallet wallet, @Nonnull BrokerAdapter adapter) {
        return new SlidingMinMaxStrategy(new TickerWithInterval(ticker[0], interval[0]), adapter, wallet, new BigDecimal("0.002"));
    }
}
