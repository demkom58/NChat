package com.demkom58.nchat.client.network;

import com.demkom58.nchat.client.controller.ChatController;
import com.demkom58.nchat.common.network.handler.PacketEncoder;
import com.demkom58.nchat.common.network.packets.CommonPacketProcessor;
import com.demkom58.nchat.common.network.packets.client.CAuthPacket;
import com.demkom58.nchat.common.network.packets.common.ADisconnectPacket;
import com.demkom58.nchat.common.network.packets.common.AMessagePacket;
import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ClientPacketProcessor extends CommonPacketProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPacketProcessor.class.getTypeName());
    private final ChatController chatController;

    public ClientPacketProcessor(ChatController chatController, Channel channel) {
        super(channel);
        this.chatController = chatController;
    }

    @Override
    public void processCAuthPacket(CAuthPacket packet) {
        LOGGER.info("Unsupported packet {}.", packet.getClass().getTypeName());
    }

    @Override
    public void processAMessagePacket(AMessagePacket packet) {
        final String msg = packet.getMessage().trim().replace(PacketEncoder.getFrameSymbol(), "");

        Platform.runLater(() -> {
            final ListView<String> messagesView = chatController.getMessagesView();
            messagesView.getItems().add(msg);
            messagesView.lookupAll(".scroll-bar").stream()
                    .filter(br -> Objects.equals(Orientation.VERTICAL, ((ScrollBar) br).getOrientation()))
                    .findFirst().ifPresent(br -> br.visibleProperty().addListener(
                            (observable, oldValue, newValue) -> messagesView.scrollTo(newValue ? Integer.MAX_VALUE : 0)
                    )
            );
        });

        LOGGER.info(msg.replace("\n", " "));
    }

    @Override
    public void processADisconnectPacket(ADisconnectPacket packet) {
        // TODO: Switch to disconnect screen.
    }

}
