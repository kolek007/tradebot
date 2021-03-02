package org.nl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StrategyConfig {
    String name;
    String tickers;
    String intervals;
    Wallet wallet;

    @JsonCreator
    public StrategyConfig(
            @JsonProperty("name") String name,
            @JsonProperty("tickers") String tickers,
            @JsonProperty("intervals") String intervals,
            @JsonProperty("wallet") Wallet wallet
    ) {
        this.name = name;
        this.tickers = tickers;
        this.intervals = intervals;
        this.wallet = wallet;
    }


    @Getter
    @ToString
    public static class Wallet {
        double amount;

        @JsonCreator
        public Wallet(@JsonProperty("amount") double amount) {
            this.amount = amount;
        }
    }
}
