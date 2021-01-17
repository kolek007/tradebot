package org.nl.linear_algebra;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor(staticName = "create")
@Getter
public class Point {
    public static final Point NA = Point.create(BigDecimal.valueOf(-1), BigDecimal.valueOf(-1));

    private final BigDecimal x;
    private final BigDecimal y;
}
