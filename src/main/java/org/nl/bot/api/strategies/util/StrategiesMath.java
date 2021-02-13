package org.nl.bot.api.strategies.util;

import org.nl.bot.api.beans.Candle;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;

public class StrategiesMath {

    /**
     * Sorts candles by height and returns height of element at 0.75 * size position
     */
    public static BigDecimal thirdQuartile(@Nonnull List<Candle> candles) {
        List<BigDecimal> sorted = candles.stream().map(StrategiesMath::height).sorted().collect(Collectors.toList());
        int size = sorted.size();
        return sorted.get(3 * size / 4);
    }

    /**
     * Sorts candles by height and returns height of element at middle position
     */
    public static BigDecimal median(@Nonnull List<Candle> candles) {
        List<BigDecimal> sorted = candles.stream().map(StrategiesMath::height).sorted().collect(Collectors.toList());
        int size = sorted.size();
        return sorted.get(size / 2);
    }

    /**
     * Returns average height of candles
     */
    public static BigDecimal average(@Nonnull List<Candle> candles) {
        BigDecimal sum = candles.stream().map(StrategiesMath::height).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(candles.size()), MathContext.DECIMAL32);
    }

    /**
     * Returns absolute difference between closing and opening prices. Always >= 0
     */
    @Nonnull
    public static BigDecimal height(@Nonnull Candle c) {
        return c.getClosingPrice().subtract(c.getOpenPrice()).abs();
    }

    /**
     * Returns value upper shadow
     */
    @Nonnull
    public static BigDecimal upperShadow(@Nonnull Candle c) {
        return c.getHighestPrice().subtract(c.getClosingPrice().max(c.getOpenPrice()));
    }

    /**
     * Returns value upper shadow
     */
    @Nonnull
    public static BigDecimal lowerShadow(@Nonnull Candle c) {
        return c.getClosingPrice().min(c.getOpenPrice()).subtract(c.getLowestPrice());
    }

    /**
     * Return true if closing price greater than opening price
     */
    public static boolean isGrowth(@Nonnull Candle c) {
        return c.getClosingPrice().compareTo(c.getOpenPrice()) > 0;
    }

    /**
     * Return true if closing price greater than opening price
     */
    public static boolean green(@Nonnull Candle c) {
        return isGrowth(c);
    }

    /**
     * Return true if closing price less than opening price
     */
    public static boolean red(@Nonnull Candle c) {
        return !isGrowth(c);
    }

    /**
     * Checks that two values are equal up to a given error
     */
    public static boolean weakEqual(@Nonnull BigDecimal first, @Nonnull BigDecimal second, double error) {
        return first.subtract(second).abs().compareTo(first.multiply(BigDecimal.valueOf(error))) <= 0;
    }
}
