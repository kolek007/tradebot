package org.nl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class StartupConfig {

    BrokerConfig broker;
    StrategyConfig[] strategies;

    @JsonCreator
    public StartupConfig(
            @JsonProperty("broker") BrokerConfig broker,
            @JsonProperty("strategies") StrategyConfig[] strategies
    ) {
        this.broker = broker;
        this.strategies = strategies;
    }


    @Getter
    @ToString
    public static class BrokerConfig {
        String name;
        String apiKey;
        String secret;
        boolean sandbox;

        @JsonCreator
        public BrokerConfig(
                @JsonProperty("name") String name,
                @JsonProperty("apiKey") String apiKey,
                @JsonProperty("secret") String secret,
                @JsonProperty("sandbox") boolean sandbox
        ) {
            this.name = name;
            this.apiKey = apiKey;
            this.secret = secret;
            this.sandbox = sandbox;
        }
    }

}
