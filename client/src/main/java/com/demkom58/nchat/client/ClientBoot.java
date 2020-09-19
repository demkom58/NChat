package com.demkom58.nchat.client;

import com.demkom58.nchat.client.application.Client;
import com.demkom58.nchat.common.Environment;
import com.sun.javafx.application.LauncherImpl;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import javafx.scene.Node;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.InjectionPointLazyFxControllerAndViewResolver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class ClientBoot {
    private static OptionSet options;

    public static void main(@NotNull final String... args) {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        options = new OptionParser().parse(args);

        LauncherImpl.launchApplication(Client.class, args);
        LoggerFactory.getLogger(ClientBoot.class).info("NChat v{} is launching.", Environment.APP_VERSION);
    }

    @Bean
    public FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {
       return new SpringFxWeaver(applicationContext);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <C, V extends Node> FxControllerAndView<C, V> controllerAndView(FxWeaver fxWeaver,
                                                                           InjectionPoint injectionPoint) {
        return new InjectionPointLazyFxControllerAndViewResolver(fxWeaver)
                .resolve(injectionPoint);
    }

    @Bean
    @Description("Arguments with which the application was launched")
    public @NotNull OptionSet startOptions() {
        return options;
    }

}
