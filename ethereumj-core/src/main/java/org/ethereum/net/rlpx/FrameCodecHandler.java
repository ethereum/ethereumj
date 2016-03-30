package org.ethereum.net.rlpx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.ethereum.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * The Netty handler responsible for decrypting/encrypting RLPx frames
 * with the FrameCodec crated during HandshakeHandler initial work
 *
 * Created by Anton Nashatyrev on 15.10.2015.
 */
public class FrameCodecHandler extends NettiByteToMessageCodec<FrameCodec.Frame> {
    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    public FrameCodec frameCodec;
    public Channel channel;

    public FrameCodecHandler(FrameCodec frameCodec, Channel channel) {
        this.frameCodec = frameCodec;
        this.channel = channel;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        if (in.readableBytes() == 0) {
            loggerWire.trace("in.readableBytes() == 0");
            return;
        }

        loggerWire.trace("Decoding frame (" + in.readableBytes() + " bytes)");
        List<FrameCodec.Frame> frames = frameCodec.readFrames(in);


        // Check if a full frame was available.  If not, we'll try later when more bytes come in.
        if (frames == null || frames.isEmpty()) return;

        for (int i = 0; i < frames.size(); i++) {
            FrameCodec.Frame frame = frames.get(i);

            channel.getNodeStatistics().rlpxInMessages.add();
        }

        out.addAll(frames);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, FrameCodec.Frame frame, ByteBuf out) throws Exception {

        frameCodec.writeFrame(frame, out);

        channel.getNodeStatistics().rlpxOutMessages.add();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (channel.isDiscoveryMode()) {
            loggerNet.debug("FrameCodec failed: ", cause);
        } else {
            if (cause instanceof IOException) {
                loggerNet.info("FrameCodec failed: " + ctx.channel().remoteAddress() + "(" + cause.getMessage() + ")");
                loggerNet.debug("FrameCodec failed: " + ctx.channel().remoteAddress(), cause);
            } else {
                loggerNet.error("FrameCodec failed: ", cause);
            }
        }
        ctx.close();
    }
}
