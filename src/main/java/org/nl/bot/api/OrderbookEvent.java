package org.nl.bot.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.nl.bot.api.beans.Orderbook;

@RequiredArgsConstructor
@Getter
public class OrderbookEvent {
    private final Orderbook orderbook;
}
