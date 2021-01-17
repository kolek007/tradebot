package org.nl.bot.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class BotManager {
    @Nonnull
    private final ConcurrentHashMap<String, Strategy> runningBots = new ConcurrentHashMap<>();

    public void run(@Nonnull Strategy strategy) throws Exception {
        runningBots.put(strategy.getId(), strategy);
        Executors.newSingleThreadExecutor().submit(strategy);
    }

    public void stop(@Nonnull String name) throws Exception {
        final Strategy strategy = runningBots.remove(name);
        if(strategy != null) {
            strategy.stop();
        }
    }

    public void destroy() {
        final Iterator<Strategy> iterator = runningBots.values().iterator();
        while (iterator.hasNext()) {
            final Strategy strategy = iterator.next();
            iterator.remove();
            try {
                strategy.stop();
            } catch (Exception e) {
                log.error("Failed to stop strategy " + strategy.getId(), e);
            }
        }
    }

}
