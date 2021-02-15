package org.nl.util;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.nl.bot.api.Operation;
import org.nl.bot.api.beans.Order;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class BrokerAssert extends AbstractAssert<BrokerAssert, BrokerMock> {
    public BrokerAssert(BrokerMock brokerMock, Class<?> selfType) {
        super(brokerMock, selfType);
    }

    public static BrokerAssert assertThat(BrokerMock actual) {
        return new BrokerAssert(actual, BrokerAssert.class);
    }

    @Nonnull
    public BrokerAssert enteredAt(BigDecimal price) {
        Assertions.assertThat(actual.getOrders()).as("Enter price").extracting(Order::getOperation,Order::getPrice).containsOnlyOnce(new Tuple(Operation.Buy,price));
        return this;
    }

    @Nonnull
    public BrokerAssert enteredAt(String price) {
        return enteredAt(new BigDecimal(price));
    }

    @Nonnull
    public BrokerAssert exitedAt(String price) {
        return exitedAt(new BigDecimal(price));
    }


    @Nonnull
    public BrokerAssert exitedAt(BigDecimal price) {
        Assertions.assertThat(actual.getOrders()).as("Exit price").extracting(Order::getOperation,Order::getPrice).containsOnlyOnce(new Tuple(Operation.Sell,price));
        return this;
    }

    @Nonnull
    public BrokerAssert notExited() {
         Assertions.assertThat(actual.getOrders()).as("No exit").extracting(Order::getOperation).doesNotContain(Operation.Sell);

         return this;
    }
}
