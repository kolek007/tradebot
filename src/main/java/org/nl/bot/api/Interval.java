package org.nl.bot.api;

public enum Interval {
    MIN_1(1), MIN_5(5), MIN_15(15), MIN_30(30), HOUR_1(60), DAY_1(24*60), YEAR_1(24*60*365);

    private final int minutes;

    Interval(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return "Interval{" +
                "minutes=" + minutes +
                '}';
    }
}
