package com.demkom58.nchat.server.network;

import com.demkom58.nchat.common.network.IPacketHandlerRegistry;
import com.demkom58.nchat.common.network.PacketHandlerRegistry;
import com.demkom58.nchat.common.network.PacketRegistry;
import com.demkom58.nchat.common.network.handler.PacketCodec;
import com.demkom58.nchat.common.network.handler.PacketEncoder;
import com.demkom58.nchat.server.application.Server;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final ApplicationContext context;
    private final PacketRegistry packetRegistry;

    @Autowired
    public ServerInitializer(ApplicationContext context, PacketRegistry packetRegistry) {
        this.context = context;
        this.packetRegistry = packetRegistry;
    }

    @Override
    public void initChannel(@NotNull SocketChannel ch) {
        final Server server = context.getBean(Server.class);

        IPacketHandlerRegistry packetProcessorRegistry = new PacketHandlerRegistry();
        packetProcessorRegistry.registerPacketProcessor(new ServerPacketProcessor(server, ch));

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB));
        pipeline.addLast("inflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.ZLIB));

        //TODO: timeout

        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(30000, Unpooled.wrappedBuffer(PacketEncoder.getSymbolBytes())));
        pipeline.addLast("codec", new PacketCodec(packetRegistry));
        pipeline.addLast("handler", new ServerPacketHandler(server, packetProcessorRegistry));
    }
}
