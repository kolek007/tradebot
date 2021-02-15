package org.nl.bot.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.nl.bot.api.beans.PlacedOrder;

@RequiredArgsConstructor
@Getter
@ToString
public class OrderUpdateEvent {
    private final PlacedOrder order;
}
