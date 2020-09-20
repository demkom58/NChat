package com.demkom58.nchat.server.network;

import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.IPacketHandlerRegistry;
import com.demkom58.nchat.common.network.handler.PacketHandler;
import com.demkom58.nchat.common.network.util.NetworkUtil;
import com.demkom58.nchat.server.application.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ServerPacketHandler extends PacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPacketHandler.class);
    private final Server server;

    public ServerPacketHandler(Server server, IPacketHandlerRegistry packetProcessorRegistry) {
        super(packetProcessorRegistry);
        this.server = server;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);

        Collection<User> users = server.getUsers();

        int connections = 0;

        String address = NetworkUtil.getAddress(ctx.channel()).split(":")[0];
        Channel incoming = ctx.channel();

        for (User user : users) {
            if (address.equals(user.getIP()))
                connections += 1;

            if (connections < Environment.CONNECTIONS_PER_IP)
                continue;

            String reason = "Too many connections.";

            User.sendMessage(incoming, reason);
            User.sendMessage(incoming, "You was kicked.");

            server.kickUser(incoming, reason);
            return;
        }

        server.getChannels().add(incoming);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (!server.getChannels().contains(ctx.channel())
                && !server.getRegisteredChannels().contains(ctx.channel()))
            return;

        server.removeUser(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();

        String reason = "Connection[" + NetworkUtil.getAddress(channel) + "] Exception caught: " + cause.getMessage();
        LOGGER.warn(reason);

        handlerRemoved(ctx);
    }

}
