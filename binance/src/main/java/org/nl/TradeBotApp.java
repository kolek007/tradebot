package org.nl;

import org.nl.cfg.BinanceBotCfg;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {BinanceBotCfg.class})
public class TradeBotApp {
    public static void main(String[] args) {
        SpringApplication.run(TradeBotApp.class, args);
    }

}
