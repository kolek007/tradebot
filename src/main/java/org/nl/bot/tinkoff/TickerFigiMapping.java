package org.nl.bot.tinkoff;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TickerFigiMapping {
    @Nonnull
    private final Map<String, String> ticker2Figi = new HashMap<>();
    @Nonnull
    private final Map<String, String> figi2Ticker = new HashMap<>();

    public void put(@Nonnull String ticker, @Nonnull String figi) {
        ticker2Figi.put(ticker, figi);
        figi2Ticker.put(figi, ticker);
    }

    @Nonnull
    public String getTicker(@Nonnull String figi) {
        if(!figi2Ticker.containsKey(figi)) {
            throw new RuntimeException("Couldn't map figi {" + figi + "} to any ticker");
        }
        return figi2Ticker.get(figi);
    }

    @Nonnull
    public String getFigi(@Nonnull String ticker) {
        if(!ticker2Figi.containsKey(ticker)) {
            throw new RuntimeException("Couldn't map ticker {" + ticker + "} to any figi");
        }
        return ticker2Figi.get(ticker);
    }
}
