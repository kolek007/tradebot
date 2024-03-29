package org.nl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class StartupConfig {

    Broker broker;
    StrategyConfig[] strategies;

    @JsonCreator
    public StartupConfig(
            @JsonProperty("broker") Broker broker,
            @JsonProperty("strategies") StrategyConfig[] strategies
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

}
