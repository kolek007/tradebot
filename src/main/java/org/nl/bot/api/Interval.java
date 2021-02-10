package org.nl.bot.api;

public enum Interval {
    MIN_1("1_min"), MIN_5("5_min"), MIN_15("15_min"), MIN_30("30_min"), HOUR_1("1_hour"), DAY_1("1_day"), WEEK("1_week"), MONTH("1_month");

    private final String name;

    Interval(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Interval{" +
                "name='" + name + '\'' +
                '}';
    }
}
