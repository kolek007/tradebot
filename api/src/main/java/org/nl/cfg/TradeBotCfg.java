package org.nl.cfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nl.StartupConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class TradeBotCfg {
    @Value("${org.nl.startup.config}")
    private String startupConfigPath;

    @Bean
    StartupConfig startupConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Paths.get(startupConfigPath).toFile(), StartupConfig.class);
    }
}
