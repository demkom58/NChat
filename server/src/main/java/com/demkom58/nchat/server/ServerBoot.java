package com.demkom58.nchat.server;

import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.server.application.Server;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;
import java.util.Arrays;

@SpringBootApplication
public class ServerBoot {
    public static OptionSet options;
    private static ConfigurableApplicationContext context;

    public static void main(@NotNull final String... args) throws Exception {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        final OptionParser optionParser = new OptionParser();
        optionParser.acceptsAll(Arrays.asList("h", "host")).withRequiredArg();
        optionParser.acceptsAll(Arrays.asList("p", "port")).withRequiredArg();
        optionParser.accepts("help").forHelp();
        options = optionParser.parse(args);

        LoggerFactory.getLogger(ServerBoot.class).info("NChat v{} is launching.", Environment.APP_VERSION);

        context = new SpringApplicationBuilder()
                .sources(ServerBoot.class)
                .run(args);

        context.getBean(Server.class).start();
    }

    @Bean
    public InetSocketAddress bindAddress() {
        String host = options.has("host")
                ? (String) options.valueOf("host")
                : null;

        int port = options.has("port")
                ? Integer.parseInt((String) options.valueOf("port"))
                : Environment.PORT;

        return host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
    }

    @Bean
    public OptionSet applicationOptions() {
        return options;
    }

    @Bean
    public ApplicationContext context() {
        return context;
    }

}
