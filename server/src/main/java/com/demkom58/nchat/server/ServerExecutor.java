package com.demkom58.nchat.server;

import com.demkom58.nchat.common.Environment;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;

@UtilityClass
public class ServerExecutor {
    public static OptionSet options;

    public static void main(@NotNull final String... args) throws Exception {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        final OptionParser optionParser = new OptionParser();
        optionParser.acceptsAll(Arrays.asList("h", "host")).withRequiredArg();
        optionParser.acceptsAll(Arrays.asList("p", "port")).withRequiredArg();
        optionParser.accepts("help").forHelp();
        options = optionParser.parse(args);

        String host = options.has("host")
                ? (String) options.valueOf("host")
                : null;

        int port = options.has("port")
                ? Integer.parseInt((String) options.valueOf("port"), Environment.PORT)
                : Environment.PORT;

        InetSocketAddress address = host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
        LoggerFactory.getLogger(ServerExecutor.class).info("NChat v{} is launching.", Environment.APP_VERSION);
        new Server(address).start();
    }
}
