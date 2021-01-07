package org.nl.bot.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.List;

@Builder
@Getter
@ToString
public class TickerEvent {
    @Nullable
    private final Candle candle;
    @Nullable
    @Singular("placedOrder")
    private final List<PlacedOrder> placedOrderList;
}
