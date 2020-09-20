package com.demkom58.nchat.server.network;

import com.demkom58.nchat.common.Environment;
import com.demkom58.nchat.common.network.handler.PacketEncoder;
import com.demkom58.nchat.common.network.packets.CommonPacketProcessor;
import com.demkom58.nchat.common.network.packets.client.CAuthPacket;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import com.demkom58.nchat.common.network.util.NetworkUtil;
import com.demkom58.nchat.server.application.Server;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ServerPacketProcessor extends CommonPacketProcessor {
    public static final Logger LOGGER = LoggerFactory.getLogger(ServerPacketProcessor.class);
    private final Server server;

    public ServerPacketProcessor(Server server, Channel channel) {
        super(channel);
        this.server = server;
    }

    @Override
    public void processCAuthPacket(CAuthPacket packet) {
        final Channel channel = getChannel();

        Collection<Channel> regChannels = server.getRegisteredChannels();
        if (regChannels.contains(channel)) {
            server.kickUser(channel, "Already registered.");
            return;
        }

        String protocolVersion, nick;

        protocolVersion = packet.getProtocolVersion();
        nick = packet.getNick();

        final User localUser = new User(server, channel, nick);
        if (!protocolVersion.equals(Environment.PROTOCOL_VERSION)) {
            localUser.sendMessage("Server protocol version and client protocol version are different.");
            localUser.sendMessage("Your protocol version: " + protocolVersion + ". Server protocol version: " + Environment.PROTOCOL_VERSION);
            localUser.kick("Different protocol versions. ClientMessenger version: " + protocolVersion + ".");
            return;
        }

        Collection<User> users = server.getUsers();
        for (User cUser : users)
            if (cUser.getNick().equals(nick)) {
                User.sendMessage(channel, "This nickname is already taken.");
                User.sendMessage(channel, "You was kicked.");
                localUser.kick("This nick already taken.");
                return;
            }

        String voidName = nick.replace(" ", "");
        if (voidName.length() == 0 || nick.length() > 16) {
            localUser.kick("Bad nick format.");
            return;
        }

        server.registerUser(channel, localUser);
        localUser.sendMessage("Now you are connected to the chat server.");
        ServerPacketProcessor.LOGGER.info("User " + localUser.getNick() + "[" + localUser.getAddress() + "] has joined.");

        for (User cUser : users)
            if (cUser.getChannel() != channel)
                cUser.sendMessage("[Server] " + server.getUser(channel).getNick() + " has joined!");
    }

    @Override
    public void processAMessagePacket(AMessagePacket packet) {
        Channel channel = getChannel();

        String message = packet.getMessage().trim().replace(PacketEncoder.getFrameSymbol(), "");
        User eventUser = server.getUser(channel);

        if (eventUser == null) {
            User.sendMessage(channel, "Register first please!");
            User.sendMessage(channel, "You was kicked!");

            server.kickUser(channel, "User wasn't registered.");
            return;
        }

        String logMessage = message.replace("\n", " ");
        if (logMessage.replace(" ", "").length() < 1) {
            eventUser.sendMessage("Too short message.");
            return;
        }

        Collection<User> users = server.getUsers();
        if (System.currentTimeMillis() < eventUser.getLastSentMessageTime()) {
            eventUser.sendMessage("[Server] Too many messages in second!");
            return;
        }
        eventUser.setLastSentMessageTime(System.currentTimeMillis() + (1000 / Environment.MESSAGES_PER_SECOND));

        if (message.length() > Environment.MAX_MESSAGE_LENGTH) {
            eventUser.sendMessage("Your message bigger then " + Environment.MAX_MESSAGE_LENGTH + " symbols. Message was cancelled.");
            LOGGER.info("User " + eventUser.getNick() + " tried to send message with length bigger then " + Environment.MAX_MESSAGE_LENGTH);
            return;
        }

        for (User user : users)
            user.sendMessage("[" + eventUser.getNick() + "] " + message);

        LOGGER.info("[" + eventUser.getAddress() + "] [" + eventUser.getNick() + "] " + logMessage);
    }

    @Override
    public void processADisconnectPacket(ADisconnectPacket packet) {
        Channel channel = getChannel();
        String reason = packet.getReason();

        String address = "[" + NetworkUtil.getAddress(channel) + "]";
        String log;

        if (!server.getRegisteredChannels().contains(channel)) {
            server.removeUser(channel);
            log = "Connection" + address + " has lost server.";
        } else {
            User user = server.getUser(channel);
            server.broadcast("[Server] " + user.getNick() + " has left!");
            server.removeUser(channel);
            log = "User " + user.getNick() + address + " has left server.";
        }
        log += " Reason: " + reason;
        ServerPacketProcessor.LOGGER.info(log);
    }
}
