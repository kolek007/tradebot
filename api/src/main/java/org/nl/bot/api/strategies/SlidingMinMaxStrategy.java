package org.nl.bot.api.strategies;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.BrokerAdapter;
import org.nl.bot.api.Operation;
import org.nl.bot.api.TickerWithInterval;
import org.nl.bot.api.Wallet;
import org.nl.bot.api.beans.impl.OrderImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
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
            BigDecimal closingPrice = candleEvent.getCandle().getClosingPrice();
            if(enteredAt == null) {
                if(currentMin == null) {
                    currentMin = closingPrice;
                } else {
                    BigDecimal subtract = closingPrice.subtract(currentMin);
                    log.info("Price changed [min={}, closing={}, diff={}]", currentMin, closingPrice, subtract);
                    if (currentMin.compareTo(closingPrice) > 0) {
                        currentMin = closingPrice;
                        log.info("Down [diff={}]", subtract);
                    } else {
                        if (subtract.compareTo(currentMin.multiply(threshold)) >= 0) {
                            log.info("Enter at [price={}, percent={}, min={}, diff={}]",
                                    closingPrice, currentMin.multiply(threshold), currentMin, subtract);
                            adapter.placeOrder(getId(), candleEvent.getCandle().getTicker(), new OrderImpl(0.001, Operation.Buy, closingPrice), null);
                            currentMax = closingPrice;
                            currentMin = null;
                            enteredAt = closingPrice;
                        } else {
                            log.info("Up [closing-price={}, percent={}, min={}, diff={}]",
                                    closingPrice, currentMin.multiply(threshold), currentMin, subtract);
                        }
                    }
                }
                return;
            }

            if(currentMax != null) {
                BigDecimal subtract = closingPrice.subtract(currentMax);
                log.info("Price changed [max={}, closing={}, entered-at={}, diff={}]", currentMax, closingPrice, enteredAt, subtract);
                if (currentMax.compareTo(closingPrice) < 0) {
                    currentMax = closingPrice;
                    log.info("Up [diff={}]", subtract);
                } else {
                    if (currentMax.multiply(threshold).compareTo(subtract.abs()) <= 0) {
                        log.info("Exit at [closing-price={}, revenue={}, percent={}, max={}, diff={}]",
                                closingPrice, closingPrice.subtract(enteredAt), currentMax.multiply(threshold), currentMax, subtract);
                        adapter.placeOrder(getId(), candleEvent.getCandle().getTicker(), new OrderImpl(1, Operation.Sell, closingPrice), null);
                        currentMax = null;
                        enteredAt = null;
                    } else {
                        log.info("Down [closing-price={}, revenue={}, percent={}, max={}, diff={}]",
                                closingPrice, closingPrice.subtract(enteredAt), currentMax.multiply(threshold), currentMax, subtract);
                    }
                }
            }
        });
    }

}
