package org.nl.bot.tinkoff.beans;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.nl.bot.api.Orderbook;
import org.nl.bot.tinkoff.TickerFigiMapping;
import ru.tinkoff.invest.openapi.models.market.TradeStatus;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
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
