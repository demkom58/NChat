package com.demkom58.nchat.client.network;

import com.demkom58.nchat.client.Client;
import com.demkom58.nchat.common.network.IPacketHandlerRegistry;
import com.demkom58.nchat.common.network.PacketHandlerRegistry;
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

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final Client client;

    public ClientInitializer(Client client) {
        this.client = client;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        IPacketHandlerRegistry packetProcessorRegistry = new PacketHandlerRegistry();
        packetProcessorRegistry.registerPacketProcessor(new ClientPacketProcessor(ch, client));

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("deflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB));
        pipeline.addLast("inflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.ZLIB));

        pipeline.addLast(new DelimiterBasedFrameDecoder(30000, Unpooled.wrappedBuffer(PacketEncoder.getSymbolBytes())));

        pipeline.addLast(new PacketCodec(ClientMessenger.getClientMessenger().getPacketRegistry()));
        pipeline.addLast(new PacketHandler(packetProcessorRegistry));
    }
}