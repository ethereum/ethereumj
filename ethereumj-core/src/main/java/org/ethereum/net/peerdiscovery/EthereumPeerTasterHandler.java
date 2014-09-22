package org.ethereum.net.peerdiscovery;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.Command;
import org.ethereum.net.PeerListener;
import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.HelloMessage;
import org.ethereum.net.message.PeersMessage;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.net.Command.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class EthereumPeerTasterHandler extends ChannelInboundHandlerAdapter {

    private final static Logger logger = LoggerFactory.getLogger("peerdiscovery");

    private final static byte[] MAGIC_PREFIX = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91};

    private long lastPongTime = 0;
    private boolean tearDown = false;

    private PeerListener peerListener;

    private HelloMessage handshake = null;

    public EthereumPeerTasterHandler() {    }

    public EthereumPeerTasterHandler(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // Here we send the hello message with random id each time
        // to not interrupt active peer

        HelloMessage helloMessage = StaticMessages.HELLO_MESSAGE;

        byte[] helloLength =ByteUtil.calcPacketLength(helloMessage.getPayload());

        final ByteBuf buffer = ctx.alloc().buffer(helloMessage.getPayload().length + 8);

        buffer.writeBytes(MAGIC_PREFIX);
        buffer.writeBytes(helloLength);
        buffer.writeBytes(helloMessage.getPayload());
        logger.info("Send: " + helloMessage.toString());
        ctx.writeAndFlush(buffer);

    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] payload = (byte[]) msg;

        logger.info("[Send msg: [{}] ]", Hex.toHexString(payload));

        byte command = RLP.getCommandCode(payload);

        // got HELLO
        if (Command.fromInt(command) == HELLO) {
            logger.info("[Recv: HELLO]" );
            RLPList rlpList = RLP.decode2(payload);
            
            HelloMessage helloMessage = new HelloMessage(rlpList);
            handshake = helloMessage;
            logger.info(helloMessage.toString());

            sendGetPeers(ctx);
        }
        // got DISCONNECT
        if (Command.fromInt(command) == DISCONNECT) {
            logger.info("[Recv: DISCONNECT]");
            if (peerListener != null) peerListener.console("[Recv: DISCONNECT]");

            RLPList rlpList = RLP.decode2(payload);
            DisconnectMessage disconnectMessage = new DisconnectMessage(rlpList);

            logger.info(disconnectMessage.toString());
        }

        // got PING send pong
        if (Command.fromInt(command) == PING) {
            logger.info("[Recv: PING]");
            if (peerListener != null) peerListener.console("[Recv: PING]");
            sendPong(ctx);
        }

        // got PONG mark it
        if (Command.fromInt(command) == PONG) {
            logger.info("[Recv: PONG]" );
            if (peerListener != null) peerListener.console("[Recv: PONG]");
            this.lastPongTime = System.currentTimeMillis();
        }

        // got PEERS
        if (Command.fromInt(command) == PEERS) {
            logger.info("[Recv: PEERS]");
            if (peerListener != null) peerListener.console("[Recv: PEERS]");

            RLPList rlpList = RLP.decode2(payload);
            PeersMessage peersMessage = new PeersMessage(rlpList);

            WorldManager.getInstance().addPeers(peersMessage.getPeers());
            logger.info(peersMessage.toString());

            sendDisconnectNice(ctx);

            ctx.close().sync();
            ctx.disconnect().sync();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // limit the size of recieving buffer to 1024
        ctx.channel().config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        ctx.channel().config().setOption(ChannelOption.SO_RCVBUF, 32368);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.tearDown = true;
        logger.info("Lost connection to the server");
    	logger.error(cause.getMessage(), cause);

        ctx.close().sync();
        ctx.disconnect().sync();
    }

    private void sendPing(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.PING_PACKET.length);
        buffer.writeBytes(StaticMessages.PING_PACKET);
        ctx.writeAndFlush(buffer);
    }

    private void sendPong(ChannelHandlerContext ctx) {
        logger.info("[Send: PONG]");
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.PONG_PACKET.length);
        buffer.writeBytes(StaticMessages.PONG_PACKET);
        ctx.writeAndFlush(buffer);
    }

    private void sendDisconnectNice(ChannelHandlerContext ctx) {
        logger.info("[Send: DISCONNECT]");
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.DISCONNECT_08.length);
        buffer.writeBytes(StaticMessages.DISCONNECT_08);
        ctx.writeAndFlush(buffer);
    }

    private void sendGetPeers(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.GET_PEERS_PACKET.length);
        buffer.writeBytes(StaticMessages.GET_PEERS_PACKET);
        ctx.writeAndFlush(buffer);
    }

    public HelloMessage getHandshake(){
        return this.handshake;
    }


}