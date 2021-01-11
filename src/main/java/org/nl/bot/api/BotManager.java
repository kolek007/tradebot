package org.nl.bot.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RequiredArgsConstructor
public class BotManager {
    @Nonnull
    private final BrokerAdapter adapter;
    @Nonnull
    private final CopyOnWriteArrayList<Bot> runningBots = new CopyOnWriteArrayList<>();

    public void run(@Nonnull Strategy strategy) {

    }

    public void kill(@Nonnull Bot bot) {

    }

    public void destroy() {

    }

}
