package org.nl;


import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class TradingParameters {
    @Nonnull
    public final String ssoToken;
    @Nonnull
    public final String[] tickers;
    @Nonnull
    public final CandleInterval[] candleIntervals;
    public final boolean sandboxMode;

    public TradingParameters(@Nonnull final String ssoToken,
                             @Nonnull final String[] tickers,
                             @Nonnull final CandleInterval[] candleIntervals,
                             final boolean sandboxMode) {
        this.ssoToken = ssoToken;
        this.tickers = tickers;
        this.candleIntervals = candleIntervals;
        this.sandboxMode = sandboxMode;
    }

    @Nonnull
    public static TradingParameters fromProgramArgs(@Nonnull final String ssoTokenArg,
                                                    @Nonnull final String tickersArg,
                                                    @Nonnull final String candleIntervalsArg,
                                                    @Nonnull final String sandboxModeArg) {
        final String[] tickers = tickersArg.split(",");
        final CandleInterval[] candleIntervals = Arrays.stream(candleIntervalsArg.split(","))
                .map(TradingParameters::parseCandleInterval)
                .toArray(value -> new CandleInterval[0]);
        if (candleIntervals.length != tickers.length)
            throw new IllegalArgumentException("Количество переданных разрешающих интервалов свечей не совпадает с переданным количеством тикеров.");

        final boolean useSandbox = Boolean.parseBoolean(sandboxModeArg);

        return new TradingParameters(ssoTokenArg, tickers, candleIntervals, useSandbox);
    }

    private static CandleInterval parseCandleInterval(final String str) {
        switch (str) {
            case "1min":
                return CandleInterval.ONE_MIN;
            case "2min":
                return CandleInterval.TWO_MIN;
            case "3min":
                return CandleInterval.THREE_MIN;
            case "5min":
                return CandleInterval.FIVE_MIN;
            case "10min":
                return CandleInterval.TEN_MIN;
            default:
                throw new IllegalArgumentException("Не распознан разрешающий интервал!");
        }
    }
}
