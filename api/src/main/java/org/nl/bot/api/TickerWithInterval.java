package org.nl.bot.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode
@ToString
@Getter
public class TickerWithInterval {
    @Nonnull
    String ticker;
    @Nonnull
    Interval interval;
}
