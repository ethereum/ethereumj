package org.ethereum.net.wire;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.rlpx.FrameCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.ethereum.net.rlpx.FrameCodec.Frame;

/**
 * The PacketDecoder parses every valid Ethereum packet to a Message object
 */
@Component
@Scope("prototype")
public class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    private FrameCodec frameCodec;

    public void setFrameCodec(FrameCodec frameCodec) {
        this.frameCodec = frameCodec;
    }

    @Autowired
    WorldManager worldManager;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        Frame frame = frameCodec.readFrame(in);
        if (frame == null) return;  // here we check if the buffer was fully read
                                    // the return means read more !!!

        byte[] payload = ByteStreams.toByteArray(frame.getStream());

        if (loggerWire.isDebugEnabled())
            loggerWire.debug("Encoded: [{}]", Hex.toHexString(payload));

        Message msg = MessageFactory.createMessage((byte)frame.getType(), payload);
        
        if (loggerNet.isInfoEnabled())
            loggerNet.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), msg);

        EthereumListener listener = worldManager.getListener();
        listener.onRecvMessage(msg);

        out.add(msg);
        in.markReaderIndex();
    }


}