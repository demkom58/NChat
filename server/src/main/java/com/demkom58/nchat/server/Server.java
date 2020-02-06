package com.demkom58.nchat.server;

import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.IPacketRegistry;
import com.demkom58.nchat.common.network.PacketRegistry;
import com.demkom58.nchat.common.network.packets.client.CAuthPacket;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import com.demkom58.nchat.common.network.util.NetworkUtil;
import com.demkom58.nchat.server.network.User;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

public class Server extends SocketServer {
    private static Server server;

    private static final Logger LOGGER = LoggerFactory.getLogger("[SERVER]");
    private static final Map<Channel, User> regMap = new WeakHashMap<>();

    private final String host;
    private final int port;

    private IPacketRegistry packetRegistry;

    private Server(String host, int port) throws Exception {
        super();

        this.host = host;
        this.port = port;

        this.packetRegistry = new PacketRegistry();

        this.packetRegistry.registerPacket(CAuthPacket.class);
        this.packetRegistry.registerPacket(AMessagePacket.class);
        this.packetRegistry.registerPacket(ADisconnectPacket.class);
    }

    public void start() {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        getLogger().info("Starting server on {}:{}.", host, port);

        try {
            getLogger().info("Waiting for connections...");
            bind(new InetSocketAddress(host, port));
        } finally {
            stop();
        }

    }

    public User getUser(Channel channel) {
        return getRegisterMap().get(channel);
    }

    public void registerUser(Channel channel, User user) {
        getRegisterMap().put(channel, user);
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
        getRegisterMap().remove(channel);
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
        return getRegisterMap().values();
    }

    public Collection<Channel> getRegisteredChannels() {
        return getRegisterMap().keySet();
    }

    private Map<Channel, User> getRegisterMap() {
        return regMap;
    }

    public static Server getServer() {
        return Server.server;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static synchronized void start(OptionSet optionSet) throws Exception {
        if (server != null)
            return;

        String host = optionSet.has("host")
                ? (String) optionSet.valueOf("host")
                : Environment.HOST;

        int port = optionSet.has("port")
                ? Integer.parseInt((String) optionSet.valueOf("port"))
                : Environment.PORT;

        server = new Server(host, port);
        server.start();
    }

}