package org.nl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class StartupConfig {

    Broker broker;
    Strategy[] strategies;

    @JsonCreator
    public StartupConfig(
            @JsonProperty("broker") Broker broker,
            @JsonProperty("strategies") Strategy[] strategies
    ) {
        this.broker = broker;
        this.strategies = strategies;
    }


    @Getter
    @ToString
    public static class Broker {
        String name;
        String ssoToken;
        String sandboxSsoToken;
        boolean sandbox;

        @JsonCreator
        public Broker(
                @JsonProperty("name") String name,
                @JsonProperty("ssoToken") String ssoToken,
                @JsonProperty("sandboxSsoToken") String sandboxSsoToken,
                @JsonProperty("sandbox") boolean sandbox
        ) {
            this.name = name;
            this.ssoToken = ssoToken;
            this.sandboxSsoToken = sandboxSsoToken;
            this.sandbox = sandbox;
        }
    }

    @Getter
    @ToString
    public static class Strategy {
        String name;
        String tickers;
        String intervals;
        Wallet wallet;

        @JsonCreator
        public Strategy(
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
