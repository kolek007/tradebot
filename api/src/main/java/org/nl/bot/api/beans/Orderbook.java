package org.nl.bot.api.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

public interface Orderbook {

    @Nonnull
    String getTicker();

    int getDepth();

    @Nonnull
    List<Item> getBids();

    @Nonnull
    List<Item> getAsks();

    @RequiredArgsConstructor
    @Getter
    final class Item {

        @Nonnull
        private final BigDecimal price;

        @Nonnull
        private final BigDecimal quantity;

    }
}
