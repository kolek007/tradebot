package org.nl.bot.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OrderbookEvent {
    private final Orderbook orderbook;
}
