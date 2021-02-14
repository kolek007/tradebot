package org.nl;

import org.nl.cfg.BinanceCfg;
import org.nl.cfg.TradeBotCfg;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {TradeBotCfg.class, BinanceCfg.class})
public class TradeBotApp {
    public static void main(String[] args) {
        SpringApplication.run(TradeBotApp.class, args);
    }

}
