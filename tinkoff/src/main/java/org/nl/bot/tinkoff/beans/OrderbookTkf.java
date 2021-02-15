package org.nl.bot.tinkoff.beans;

import lombok.RequiredArgsConstructor;
import org.nl.bot.api.beans.Orderbook;
import org.nl.bot.tinkoff.TickerFigiMapping;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderbookTkf implements Orderbook {
    @Nonnull
    private final ru.tinkoff.invest.openapi.models.market.Orderbook orderbook;
    
    @Nonnull
    private final TickerFigiMapping tickerFigiMapping;
    
    @Nonnull
    @Override
    public String getTicker() {
        return tickerFigiMapping.getTicker(orderbook.figi);
    }

    @Override
    public int getDepth() {
        return orderbook.depth;
    }

    @Nonnull
    @Override
    public List<Item> getBids() {
        return orderbook.bids.stream().map(i -> new Item(i.price, i.quantity)).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<Item> getAsks() {
        return orderbook.asks.stream().map(i -> new Item(i.price, i.quantity)).collect(Collectors.toList());
    }

}
