package org.nl.bot.api;

public interface EventListener<T> {
    void onEvent(T t);
}
