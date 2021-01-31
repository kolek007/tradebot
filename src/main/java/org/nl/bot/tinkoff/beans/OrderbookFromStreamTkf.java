package org.nl.bot.tinkoff.beans;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.Orderbook;
import org.nl.bot.tinkoff.TickerFigiMapping;
import ru.tinkoff.invest.openapi.models.market.TradeStatus;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderbookFromStreamTkf implements Orderbook {
    @Nonnull
    private final StreamingEvent.Orderbook orderbook;
    
    @Nonnull
    private final TickerFigiMapping tickerFigiMapping;
    
    @Nonnull
    @Override
    public String getTicker() {
        return tickerFigiMapping.getTicker(orderbook.getFigi());
    }

    @Override
    public int getDepth() {
        return orderbook.getDepth();
    }

    @Nonnull
    @Override
    public List<Item> getBids() {
        return orderbook.getBids().stream().map(i -> new Item(i[0], i[1])).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<Item> getAsks() {
        return orderbook.getAsks().stream().map(i -> new Item(i[0], i[1])).collect(Collectors.toList());
    }

}
