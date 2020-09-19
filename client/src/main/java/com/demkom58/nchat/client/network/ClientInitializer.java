package com.demkom58.nchat.client.network;

import com.demkom58.nchat.client.controller.ChatController;
import com.demkom58.nchat.common.network.IPacketHandlerRegistry;
import com.demkom58.nchat.common.network.PacketHandlerRegistry;
import com.demkom58.nchat.common.network.PacketRegistry;
import com.demkom58.nchat.common.network.handler.PacketCodec;
import com.demkom58.nchat.common.network.handler.PacketEncoder;
import com.demkom58.nchat.common.network.handler.PacketHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final PacketRegistry packetRegistry;
    private final FxWeaver fxWeaver;

    @Autowired
    public ClientInitializer(ClientPacketRegistry packetRegistry, FxWeaver fxWeaver) {
        this.packetRegistry = packetRegistry;
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void initChannel(@NotNull SocketChannel ch) {
        IPacketHandlerRegistry packetProcessorRegistry = new PacketHandlerRegistry();
        packetProcessorRegistry.registerPacketProcessor(new ClientPacketProcessor(fxWeaver.getBean(ChatController.class), ch));

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB));
        pipeline.addLast("inflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.ZLIB));

        pipeline.addLast(new DelimiterBasedFrameDecoder(30000, Unpooled.wrappedBuffer(PacketEncoder.getSymbolBytes())));

        pipeline.addLast(new PacketCodec(packetRegistry));
        pipeline.addLast(new PacketHandler(packetProcessorRegistry));
    }
}