package org.nl.bot.api;

import javax.annotation.Nonnull;

public interface Strategy extends Runnable {
    /**
     * Must be unique
     */
    @Nonnull
    String getId();

    @Nonnull
    Wallet wallet();

    void stop() throws Exception;
}
