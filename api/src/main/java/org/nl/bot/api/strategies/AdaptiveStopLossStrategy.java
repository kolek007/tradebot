package org.nl.bot.api.strategies;

import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Operation;
import org.nl.bot.api.Wallet;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.impl.OrderImpl;
import org.nl.bot.api.strategies.util.Trend;
import org.nl.bot.api.strategies.util.TrendCalculator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Log4j2
public class AdaptiveStopLossStrategy extends AbstractStrategy {
    @Nonnull
    private final TrendCalculator trendCalculator;
    @Nonnull
    private final AdaptiveStrategyConfig config;
    @Nullable
    private BigDecimal currentMax;
    @Nullable
    private BigDecimal enteredAt;
    @Nonnull
    private Trend trend = Trend.UNKNOWN;
    @Nullable
    private Candle lastCandle;

    public AdaptiveStopLossStrategy(@Nonnull AdaptiveStrategyConfig config,
                                    @Nonnull BrokerAdapter adapter,
                                    @Nonnull Wallet wallet
                                    ) {
        super(Lists.newArrayList(config.instrument()), adapter, wallet);
        this.trendCalculator = TrendCalculator.builder().minPredictionSize(config.minPredictionSize()).build();
        this.config = config;
        log.info("Initialized with config.[cfg={}]", config);
//        Optional<List<Candle>> candles = adapter.getHistoricalCandles(config.instrument().getTicker(), OffsetDateTime.now().minusHours(24), OffsetDateTime.now(),config.instrument().getInterval()).join();
//
//        HistoryUtils.save("C:/trade_data/BA.json", candles.get());
//        List<Candle> read = HistoryUtils.read("C:/trade_data/BA.json");
    }

    @Nonnull
    @Override
    public String getId() {
        return new StringJoiner("-")
                .add(this.getClass().getName())
                .add(instruments())
                .add(UUID.randomUUID().toString()).toString();
    }

    @Override
    public void run() {
        fetchHistory();
        adapter.subscribeCandle(getId(), instruments.get(0), candleEvent -> {
            BigDecimal closingPrice = candleEvent.getCandle().getClosingPrice();

            if (lastCandle==null || candleEvent.getCandle().getDateTime().getMinute() != lastCandle.getDateTime().getMinute()) {
                log.info("Candle event. [closing-price={}, entered-at={}, diff={}]",
                        closingPrice, enteredAt, enteredAt != null ? closingPrice.subtract(enteredAt) : null);
                lastCandle = candleEvent.getCandle();
            }

            Trend newTrend = trendCalculator.calculate(closingPrice, candleEvent.getCandle().getDateTime().toEpochSecond() * 1000);

            if (newTrend == Trend.UNKNOWN) {
                log.debug("Trend is UNKNOWN yet.");
                return;
            }

            if (newTrend != trend) {
                log.info("Trend has changed. {}->{}.[closing-price={}, entered-at={}, diff={}]", trend, newTrend,
                        closingPrice, enteredAt, enteredAt != null ? closingPrice.subtract(enteredAt) : null);
                trend = newTrend;
            }

            if (!isEntered()) {
                if (trend == Trend.UP) {
                    enter(closingPrice);

                }

                return;
            }


            if (currentMax.compareTo(closingPrice) < 0) {
                BigDecimal subtract = closingPrice.subtract(currentMax);
                currentMax = closingPrice;
                log.info("New maximum [max={}, entered-at={}, revenue={}, diff={}]",currentMax, enteredAt, currentMax.subtract(enteredAt),subtract);

                if (takeProfit(closingPrice)) {
                    log.info("Exit at WITH profit [closing-price={}, revenue={}, entered-at={}, percent={}, max={}]",
                            closingPrice, closingPrice.subtract(enteredAt), enteredAt, currentMax.multiply(config.stopLoss()), currentMax);
                    exit(closingPrice);
                }
            } else {
                if (stopLoss(closingPrice)) {
                    log.info("EXIT at STOP LOSS [closing-price={}, revenue={}, entered-at={}, max={}]",
                            closingPrice, closingPrice.subtract(enteredAt), enteredAt, currentMax);
                    exit(closingPrice);
                }
            }
        });
    }

    private boolean isEntered() {
        return enteredAt != null;
    }

    private void enter(@Nonnull BigDecimal val) {
        if (enteredAt != null) {
            return;
        }

        currentMax = val;
        enteredAt = val;

        log.info("ENTER at [price={}]",enteredAt);
        adapter.placeOrder(getId(), config.instrument().getTicker(), new OrderImpl(1, Operation.Buy,val),null);
    }

    private void exit(@Nonnull BigDecimal closingPrice) {
        adapter.placeOrder(getId(),config.instrument().getTicker(), new OrderImpl(1, Operation.Sell,closingPrice),null);
        currentMax = null;
        enteredAt = null;
    }

    private boolean takeProfit(BigDecimal current) {
        if (enteredAt == null) {
            return false;
        }
        return enteredAt.multiply(config.takeProfit()).compareTo(enteredAt.subtract(current).abs()) <= 0;
    }

    private boolean stopLoss(BigDecimal current) {
        if (enteredAt == null) {
            return false;
        }

        return enteredAt.multiply(config.stopLoss()).compareTo(enteredAt.subtract(current).abs()) <= 0;
    }

    private void fetchHistory() {
        List<Candle> candles = adapter.getHistoricalCandles(config.instrument().getTicker(),
                OffsetDateTime.now().minus(trendCalculator.getTimeOffset(), ChronoUnit.MILLIS),
                OffsetDateTime.now(),
                config.instrument().getInterval()).join().get();


        double[] prices = new double[candles.size()];
        double[] times = new double[candles.size()];


        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);
            prices[i] = candle.getClosingPrice().doubleValue();
            times[i] = candle.getDateTime().toEpochSecond()*1000;
        }

        trendCalculator.reset(prices, times);
    }

}
