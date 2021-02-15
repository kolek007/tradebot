package org.nl.bot.api.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

public interface Orderbook {

    @Nonnull
    String getTicker();

    /**
     * Глубина стакана.
     */
    int getDepth();

    /**
     * Список выставленных заявок на продажу.
     */
    @Nonnull
    List<Item> getBids();

    /**
     * Список выставленных заявок на покупку.
     */
    @Nonnull
    List<Item> getAsks();

    @RequiredArgsConstructor
    @Getter
    final class Item {

        /**
         * Цена предложения.
         */
        @Nonnull
        private final BigDecimal price;

        /**
         * Количество предложений по цене.
         */
        @Nonnull
        private final BigDecimal quantity;

    }
}
