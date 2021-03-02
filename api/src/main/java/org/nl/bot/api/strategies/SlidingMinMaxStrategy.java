package org.nl.bot.api.strategies;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Operation;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;
import org.nl.bot.api.beans.impl.OrderImpl;
import org.nl.bot.api.strategies.util.StrategiesMath;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;


@Slf4j
public class SlidingMinMaxStrategy extends AbstractStrategy {

    @Nullable
    private volatile BigDecimal currentMax;
    @Nullable
    private volatile BigDecimal currentMin;
    @Nullable
    private volatile BigDecimal enteredAt;
    @Nonnull
    private final BigDecimal threshold;
    @Nonnull
    private List<BigDecimal> lastCandles = new LinkedList<>();


    public SlidingMinMaxStrategy(@Nonnull TickerWithInterval instrument,
                                 @Nonnull BrokerAdapter adapter,
                                 @Nonnull Wallet wallet,
                                 @Nonnull BigDecimal threshold
                                    ) {
        super(Lists.newArrayList(instrument), adapter, wallet);
        this.threshold = threshold;
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
        adapter.subscribeCandle(getId(), instruments.get(0), candleEvent -> {
            if (lastCandles.size() <= 61) {
                lastCandles.add(StrategiesMath.height(candleEvent.getCandle()));
                return;
            }
            lastCandles.add(StrategiesMath.height(candleEvent.getCandle()));
            lastCandles.remove(0);
            lastCandles.sort(BigDecimal::compareTo);
            BigDecimal medianHeight = lastCandles.get(30);
            BigDecimal medianThreshold = medianHeight.multiply(threshold);
//            synchronized (SlidingMinMaxStrategy.this) {
            BigDecimal closingPrice = candleEvent.getCandle().getClosingPrice();
            if (enteredAt == null) {
                if (currentMin == null) {
                    currentMin = closingPrice;
                } else {
                    BigDecimal subtract = closingPrice.subtract(currentMin);
                    log.info("Price changed [min={}, closing={}, diff={}]", currentMin, closingPrice, subtract);
                    if (currentMin.compareTo(closingPrice) > 0) {
                        currentMin = closingPrice;
                        log.info("Down [diff={}]", subtract);
                    } else {
                        if (subtract.compareTo(medianThreshold) >= 0) {
                            log.warn("Enter at [price={}, min={}, diff={}]",
                                    closingPrice, currentMin, subtract);
                            wallet.withdraw(closingPrice.multiply(BigDecimal.valueOf(0.001)));
                            adapter.placeOrder(getId(), candleEvent.getCandle().getTicker(), new OrderImpl(0.001, Operation.Buy, closingPrice), null);
                            currentMax = closingPrice;
                            currentMin = null;
                            enteredAt = closingPrice;
                        } else {
                            log.info("Up [closing-price={}, min={}, diff={}]",
                                    closingPrice, currentMin, subtract);
                        }
                    }
                }
                return;
            }

            if (currentMax != null) {
                BigDecimal diff = closingPrice.subtract(currentMax);
                log.info("Price changed [max={}, closing={}, entered-at={}, diff={}]", currentMax, closingPrice, enteredAt, diff);
                if (currentMax.compareTo(closingPrice) < 0) {
                    currentMax = closingPrice;
                    log.info("Up [diff={}]", diff);
                } else {
                    if (diff.abs().compareTo(medianThreshold) >= 0) {
                        log.warn("Exit at [closing-price={}, revenue={}, max={}, diff={}]",
                                closingPrice, closingPrice.subtract(enteredAt), currentMax, diff);
                        adapter.placeOrder(getId(), candleEvent.getCandle().getTicker(), new OrderImpl(0.001, Operation.Sell, closingPrice), null);
                        wallet.enroll(closingPrice.multiply(BigDecimal.valueOf(0.001)));
                        currentMax = null;
                        enteredAt = null;
                    } else {
                        log.info("Down [closing-price={}, revenue={}, max={}, diff={}]",
                                closingPrice, closingPrice.subtract(enteredAt), currentMax, diff);
                    }
                }
            }
//        }
        });
    }

}
