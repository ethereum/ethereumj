package org.ethereum.net.peerdiscovery;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import org.ethereum.crypto.HashUtil;
import org.ethereum.gui.PeerListener;
import org.ethereum.manager.MainData;
import org.ethereum.net.Command;
import org.ethereum.net.message.*;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.Timer;
import java.util.TimerTask;

import static org.ethereum.net.Command.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class EthereumPeerTasterHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(getClass());

    Timer timer = null;
    private final static byte[] MAGIC_PREFIX = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91};

    private final static byte[] HELLO_MESSAGE = StaticMessages.HELLO_MESSAGE.getPayload();
    private final static byte[] HELLO_MESSAGE_LEN = ByteUtil.calcPacketLength(HELLO_MESSAGE);

    private long lastPongTime = 0;
    private boolean tearDown = false;

    private PeerListener peerListener;

    public EthereumPeerTasterHandler() {    }

    public EthereumPeerTasterHandler(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // Here we send the hello message with random id each time
        // to not interrupt active peer

        byte[] peerIdBytes = HashUtil.randomPeerId();
        HelloMessage helloMessage = new HelloMessage((byte)0x11, (byte)0x00, "EthereumJ [v0.5.1] by RomanJ  ",
                (byte)0b00000111, (short)30303, peerIdBytes);
        byte[] helloLength =ByteUtil.calcPacketLength(helloMessage.getPayload());

        final ByteBuf buffer = ctx.alloc().buffer(helloMessage.getPayload().length + 8);
        timer = new Timer();

        buffer.writeBytes(MAGIC_PREFIX);
        buffer.writeBytes(helloLength);
        buffer.writeBytes(helloMessage.getPayload());
        logger.info("Send: " + helloMessage.toString());
        ctx.writeAndFlush(buffer);

        // sample for pinging in background
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {

                if (lastPongTime == 0) lastPongTime = System.currentTimeMillis();
                if (tearDown) this.cancel();

                long currTime = System.currentTimeMillis();
                if (currTime - lastPongTime > 30000){
                    logger.info("No ping answer for [30 sec]");
                    throw new Error("No ping return for 30 [sec]");
                    // TODO: shutdown the handler
                }
                logger.info("[Send: PING]");
                if (peerListener != null) peerListener.console("[Send: PING]");
                sendPing(ctx);
            }
        }, 2000, 5000);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] payload = (byte[]) msg;

        logger.info("msg: %s", Hex.toHexString(payload));


        byte command = RLP.getCommandCode(payload);

        // got HELLO
        if (Command.fromInt(command) == HELLO) {
            logger.info("[Recv: HELLO]" );
            RLPList rlpList = RLP.decode2(payload);
            
            HelloMessage helloMessage = new HelloMessage(rlpList);
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

            MainData.instance.addPeers(peersMessage.getPeers());
            logger.info(peersMessage.toString());

            sendDisconnectNice(ctx);

            timer.cancel();
            timer.purge();
            timer = null;

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
        cause.printStackTrace();
        timer.cancel();
        timer.purge();
        timer = null;

        ctx.close().sync();
        ctx.disconnect().sync();

        throw new Error("Peer is dead");
    }

    private void sendPing(ChannelHandlerContext ctx){
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.PING.length);
        buffer.writeBytes(StaticMessages.PING);
        ctx.writeAndFlush(buffer);
    }

    private void sendPong(ChannelHandlerContext ctx){
        logger.info("[Send: PONG]");
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.PONG.length);
        buffer.writeBytes(StaticMessages.PONG);
        ctx.writeAndFlush(buffer);
    }

    private void sendDisconnectNice(ChannelHandlerContext ctx){
        logger.info("[Send: DISCONNECT]");
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.DISCONNECT_00.length);
        buffer.writeBytes(StaticMessages.DISCONNECT_00);
        ctx.writeAndFlush(buffer);
    }

    private void sendGetPeers(ChannelHandlerContext ctx){
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.GET_PEERS.length);
        buffer.writeBytes(StaticMessages.GET_PEERS);
        ctx.writeAndFlush(buffer);
    }

}