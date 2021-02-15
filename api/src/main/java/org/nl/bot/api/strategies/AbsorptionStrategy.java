package org.nl.bot.api.strategies;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nl.bot.api.*;
import org.nl.bot.api.beans.Candle;
import org.nl.bot.api.beans.impl.OrderImpl;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.nl.bot.api.strategies.util.StrategiesMath.*;

@Slf4j
public class AbsorptionStrategy extends AbstractStrategy {

    public static final double ERROR_PERCENTAGE = 0.1;
    public static final double TAKE_PROFIT_PERCENTAGE = 1.1;
    public static final double STOP_LOSS_PERCENTAGE = 0.9;
    @Nonnull
    private final TickerWithInterval instrument;

    public AbsorptionStrategy(@Nonnull TickerWithInterval instrument, @Nonnull BrokerAdapter adapter, @Nonnull Wallet wallet) {
        super(Lists.newArrayList(instrument), adapter, wallet);
        this.instrument = instrument;
    }

    @Nonnull
    @Override
    public String getId() {
        return "Absorption " + instruments() + " " + UUID.randomUUID();
    }

    @Override
    public void run() {
        Optional<List<Candle>> candles = adapter.getHistoricalCandles(instrument.getTicker(), OffsetDateTime.now().minusHours(2), OffsetDateTime.now(), instrument.getInterval()).join();
        if(candles.isPresent()) {
            List<Candle> candlesList = candles.get();
            if(candlesList.isEmpty()) {
                log.error("Received zero candles from history for {}", instrument);
                return;
            }
            BigDecimal threshold = thirdQuartile(candlesList);
            adapter.subscribeCandle(getId(), instruments.get(0), new Listener(threshold));
        }
    }

    class Listener implements EventListener<CandleEvent> {

        @Nonnull
        private final BigDecimal threshold;
        Candle first = null;
        Candle second = null;

        public Listener(@Nonnull BigDecimal threshold) {
            this.threshold = threshold;
        }

        @Override
        public void onEvent(CandleEvent candleEvent) {
            log.info("Bot {} received candle event {}", getId(), candleEvent);
            Candle candle = candleEvent.getCandle();
            if(first == null) {
                first = candle;
            } else if(second == null) {
                if(!first.getDateTime().equals(candle.getDateTime())) { //we received candle for the next time period, previous candle is closed
                    if(red(first) && meetsRequirements(first)) {
                        second = candle;
                    } else {
                        first = candle;
                    }
                }
            } else if(!second.getDateTime().equals(candle.getDateTime())) { //we again received candle for the next time period, previous candle is closed
                BigDecimal firstHeight = height(first);
                BigDecimal secondHeight = height(second);
                if(green(second) && meetsRequirements(second) && weakEqual(firstHeight, secondHeight, ERROR_PERCENTAGE)) { //main strategy condition met, need to buy
                    BigDecimal price = candle.getClosingPrice();
                    BigDecimal takeProfit = price.add(secondHeight.multiply(BigDecimal.valueOf(TAKE_PROFIT_PERCENTAGE)));
                    BigDecimal stopLoss = price.subtract(secondHeight.multiply(BigDecimal.valueOf(STOP_LOSS_PERCENTAGE)));
                    adapter.placeOrder(getId(), instrument.getTicker(), new OrderImpl(1, Operation.Buy, price), null).join(); //TODO need to think about price and lots amount
                    adapter.placeOrder(getId(), instrument.getTicker(), new OrderImpl(1, Operation.Sell, takeProfit), null); //TAKE PROFIT
                    adapter.placeOrder(getId(), instrument.getTicker(), new OrderImpl(1, Operation.Sell, stopLoss), null); //STOP LOSS
                }
                //Starting new iteration of awaiting conditions
                first = candle;
                second = null;
            }

        }

        private boolean meetsRequirements(Candle first) {
            BigDecimal height = height(first);
            return height.compareTo(threshold) > 0 && //height of candle should be greater than 3/4 of all other candles 
                    weakEqual(height, upperShadow(first), ERROR_PERCENTAGE) && //shadow is not greater than 10% of candle height
                    weakEqual(height, lowerShadow(first), ERROR_PERCENTAGE); //shadow is not greater than 10% of candle height
        }
    }
}
