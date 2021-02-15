package org.nl.bot.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.nl.bot.api.beans.Orderbook;

@RequiredArgsConstructor
@Getter
@ToString
public class OrderbookEvent {
    private final Orderbook orderbook;
}
