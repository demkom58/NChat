package com.demkom58.nchat.server.application;

import com.demkom58.nchat.common.network.IPacketRegistry;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import com.demkom58.nchat.common.network.util.NetworkUtil;
import com.demkom58.nchat.server.network.ServerInitializer;
import com.demkom58.nchat.server.network.ServerPacketRegistry;
import com.demkom58.nchat.server.network.User;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

@Component
public class Server extends SocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final Map<Channel, User> regMap = new WeakHashMap<>();
    private final InetSocketAddress address;
    private final IPacketRegistry packetRegistry;

    @Autowired
    public Server(@NotNull final InetSocketAddress address,
                  @NotNull final ServerInitializer initializer,
                  @NotNull final ServerPacketRegistry packetRegistry) {
        super(initializer);

        this.address = address;
        this.packetRegistry = packetRegistry;
    }

    public void start() {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        LOGGER.info("Starting server on {}:{}.", address.getHostName(), address.getPort());

        try {
            LOGGER.info("Waiting for connections...");
            bind(address);
        } finally {
            stop();
        }

    }

    public User getUser(Channel channel) {
        return regMap.get(channel);
    }

    public void registerUser(Channel channel, User user) {
        regMap.put(channel, user);
    }

    public void kickUser(User user, String reason) {
       final Channel channel = user.getChannel();
       final ADisconnectPacket packet = new ADisconnectPacket(reason);

        channel.writeAndFlush(packet);
        LOGGER.info("User " + user.getNick() + "[" + user.getAddress() + "]" + " disconnected. Reason: " + reason);

        user.sendPacket(packet);
        removeUser(channel);
    }

    public void kickUser(Channel channel, String reason) {
        final User user = getUser(channel);
        if(user != null) {
            kickUser(user, reason);
            return;
        }
        final ADisconnectPacket packet = new ADisconnectPacket(reason);

        channel.writeAndFlush(packet);
        LOGGER.info("Guest[" + NetworkUtil.getAddress(channel) + "]" + " disconnected. Reason: " + reason);

        User.sendPacket(channel, packet);
        removeUser(channel);
    }

    public void removeUser(Channel channel) {
        getChannels().remove(channel);
        regMap.remove(channel);
        channel.close();
    }

    public void broadcast(String message) {
        final String rMessage = message + "\n";
        getRegisteredChannels().forEach(channel -> channel.writeAndFlush(rMessage));
    }

    public void stop(String reason) {
        broadcast(reason);
        shutdown();
    }

    public void stop() {
        stop("Server has been disabled.");
    }

    public IPacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    public Collection<User> getUsers() {
        return regMap.values();
    }

    public Collection<Channel> getRegisteredChannels() {
        return regMap.keySet();
    }

}
