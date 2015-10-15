package org.ethereum.net.rlpx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.ethereum.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 15.10.2015.
 */
public class MedianFrameCodec extends ByteToMessageCodec<FrameCodec.Frame> {
    private static final Logger loggerWire = LoggerFactory.getLogger("wire");
    private static final Logger loggerNet = LoggerFactory.getLogger("net");

    public FrameCodec frameCodec;
    public Channel channel;

    public MedianFrameCodec(FrameCodec frameCodec, Channel channel) {
        this.frameCodec = frameCodec;
        this.channel = channel;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        if (in.readableBytes() == 0) {
            loggerWire.debug("in.readableBytes() == 0");
            return;
        }

        loggerWire.debug("Decoding frame (" + in.readableBytes() + " bytes)");
        List<FrameCodec.Frame> frames = frameCodec.readFrames(in);


        // Check if a full frame was available.  If not, we'll try later when more bytes come in.
        if (frames == null || frames.isEmpty()) return;

        for (int i = 0; i < frames.size(); i++) {
            FrameCodec.Frame frame = frames.get(i);

            if (loggerWire.isDebugEnabled())
                loggerWire.debug("Recv: Encoded: (" + (i + 1) + " of " + frames.size() + ") " +
                        frame.getType() + " [size: " + frame.getStream().available() + "]");
        }

        out.addAll(frames);
        channel.getNodeStatistics().rlpxInMessages.add();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, FrameCodec.Frame frame, ByteBuf out) throws Exception {

        frameCodec.writeFrame(frame, out);

        channel.getNodeStatistics().rlpxOutMessages.add();
    }
}
