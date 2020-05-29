package com.demkom58.nchat.client.network;

import com.demkom58.nchat.client.Client;
import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.IPacketRegistry;
import com.demkom58.nchat.common.network.PacketRegistry;
import com.demkom58.nchat.common.network.packets.Packet;
import com.demkom58.nchat.common.network.packets.client.CAuthPacket;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientMessenger extends SocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessenger.class.getTypeName());
    private static final LinkedBlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();

    private static ClientMessenger clientMessenger;
    private static boolean work;

    private final IPacketRegistry packetRegistry = new PacketRegistry();

    private final Client client;
    private final InetSocketAddress address;

    public ClientMessenger(@NotNull final Client client, @NotNull final InetSocketAddress host) {
        super(client);

        this.client = client;
        this.address = host;

        this.packetRegistry.registerPacket(CAuthPacket.class);
        this.packetRegistry.registerPacket(AMessagePacket.class);
        this.packetRegistry.registerPacket(ADisconnectPacket.class);
    }

    public void run() throws Exception {
        ClientMessenger.work = true;
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            connect(address);
            User user = client.getUser();

            //Send register packet
            sendPacket(new CAuthPacket(user.getName(), Environment.PROTOCOL_VERSION));

            while (work) {
                String message = messagesQueue.take().replace("╥", "");
                sendPacket(new AMessagePacket(message)).syncUninterruptibly();
                user.addSentMessage();
            }

        } finally {
            group.shutdownGracefully();
        }
    }

    public IPacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    public ChannelFuture sendPacket(Packet<?> packet) {
        if (getChannel() == null)
            return null;

        return getChannel().writeAndFlush(packet);
    }

    public void sendMessage(String message) {
        messagesQueue.offer(message);
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public static ClientMessenger getClientMessenger() {
        return clientMessenger;
    }

    public static void close() {
        work = false;
    }

    public static void setup(@NotNull final Client client, @NotNull final InetSocketAddress address) {
        ClientMessenger.clientMessenger = new ClientMessenger(client, address);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

}
