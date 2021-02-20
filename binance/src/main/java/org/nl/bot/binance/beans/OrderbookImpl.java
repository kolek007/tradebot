package org.nl.bot.binance.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.nl.bot.api.beans.Orderbook;

import javax.annotation.Nonnull;
import java.util.List;

@Builder
@Getter
@ToString
public class OrderbookImpl implements Orderbook {
    @Nonnull
    private final List<Item> asks;
    @Nonnull
    private final List<Item> bids;
    @Nonnull
    private final String ticker;
    private final int depth;

}
