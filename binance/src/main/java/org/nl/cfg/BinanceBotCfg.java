package org.nl.cfg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BinanceBotCfg {
    @Value("${org.nl.startup.config}")
    private String startupConfigPath;

}
