package org.nl.bot.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public class InstrumentPerBot {
    @Nonnull
    String id;
    @Nonnull
    TickerWithInterval instrument;
}

