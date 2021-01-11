package org.nl.bot.tinkoff;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.example.unicast.AsyncSubscriber;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
@Slf4j
public class StreamingApiSubscriber extends AsyncSubscriber<StreamingEvent> {
    StreamingApiSubscriber(@Nonnull final Executor executor) {
        super(executor);
    }

    @Override
    protected boolean whenNext(final StreamingEvent event) {
        log.info("Пришло новое событие из Streaming API\n {}", event);

        return true;
    }
}
