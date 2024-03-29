package org.nl;

import org.nl.bot.tinkoff.cfg.TinkoffCfg;
import org.nl.cfg.TradeBotCfg;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {TinkoffCfg.class})
public class TradeBotApp {
    public static void main(String[] args) {
        SpringApplication.run(TradeBotApp.class, args);
    }

}
