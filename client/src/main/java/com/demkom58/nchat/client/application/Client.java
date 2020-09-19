package com.demkom58.nchat.client.application;

import com.demkom58.nchat.client.ClientBoot;
import com.demkom58.nchat.client.event.ShutdownEvent;
import com.demkom58.nchat.client.event.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class Client extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        final String[] params
                = getParameters().getRaw().toArray(new String[0]);

        final ApplicationContextInitializer<GenericApplicationContext> initializer = context -> {
            context.registerBean(Application.class, () -> Client.this);
            context.registerBean(Parameters.class, this::getParameters);
        };

        this.context = new SpringApplicationBuilder()
                .sources(ClientBoot.class)
                .initializers(initializer)
                .run(params);
    }

    @Override
    public void start(@NotNull final Stage stage) throws Exception {
        this.context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() throws Exception {
        this.context.publishEvent(new ShutdownEvent(this));
        this.context.close();
        Platform.exit();
    }

}
