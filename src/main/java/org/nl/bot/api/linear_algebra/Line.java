package org.nl.bot.api.linear_algebra;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.MathContext;

import static org.nl.bot.api.linear_algebra.Point.NA;

@RequiredArgsConstructor(staticName = "create")
@Getter
public class Line {
    public static final MathContext MC = MathContext.DECIMAL32;
    public static final BigDecimal eps = BigDecimal.valueOf(0.0000001d);
    private final Point p1;
    private final Point p2;
    @Nullable
    private BigDecimal k = null;
    @Nullable
    private BigDecimal b = null;

    @Nonnull
    public BigDecimal getK() {
        if (k == null) {
            k = (p2.getY().subtract(p1.getY(), MC)).divide(p2.getX().subtract(p1.getX(), MathContext.DECIMAL32), MathContext.DECIMAL32);
        }
        return k;
    }

    @Nonnull
    public BigDecimal getB() {
        if (b == null) {
            b = (p2.getX().multiply(p1.getY(), MC).subtract(p1.getX().multiply(p2.getY(), MC), MC)).divide(p2.getX().subtract(p1.getX(), MC), MC);
        }
        return b;
    }

    public int pointAtLine(@Nonnull Point p) {
        final BigDecimal subtract = p.getY().subtract(getK().multiply(p.getX(), MC)).subtract(getB());
        if (subtract.abs().compareTo(eps) <= 0) {
            return 0;
        }
        return subtract.compareTo(BigDecimal.ZERO);
    }

    @Nonnull
    public static Point intersection(@Nonnull Line l1, @Nonnull Line l2) {
        if(l1.getK().subtract(l2.getK()).abs().compareTo(Line.eps) <= 0) {
            return NA;
        }
        BigDecimal x = l1.getB().subtract(l2.getB()).divide(l1.getK().subtract(l2.getK()), Line.MC);
        BigDecimal y = l1.getK().multiply(x, MC).add(l1.getB());
        return Point.create(x, y);
    }
}
