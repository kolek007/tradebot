package org.nl.bot.tinkoff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.SandboxOpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Slf4j
public class OpenApiFactory {
    @Nonnull
    private final String ssoToken;
    @Nonnull
    private final Boolean sandbox;
    private final CopyOnWriteArrayList<OpenApi> connections = new CopyOnWriteArrayList<>();

    @Nonnull
    public OpenApi createConnection() {
        final Logger logger  = Logger.getLogger(OpenApiFactory.class.getName());

        final OkHttpOpenApiFactory factory = new OkHttpOpenApiFactory(ssoToken, logger);
        OpenApi api;
        log.info("Creating connection... ");
        if (sandbox) {
            api = factory.createSandboxOpenApiClient(Executors.newSingleThreadExecutor());
            // ОБЯЗАТЕЛЬНО нужно выполнить регистрацию в "песочнице"
            ((SandboxOpenApi) api).getSandboxContext().performRegistration(null).join();
        } else {
            api = factory.createOpenApiClient(Executors.newSingleThreadExecutor());
        }
        connections.add(api);
        return api;
    }

    public void destroy() {
        connections.forEach(api -> {
            try {
                log.info("Closing connection {}", api);
                api.close();
            } catch (Exception e) {
                log.error("Couldn't close connection", e);
            }
        });
    }
}
