package org.ethereum.net.client;

import static org.ethereum.net.Command.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.gui.PeerListener;
import org.ethereum.manager.MainData;
import org.ethereum.net.Command;
import org.ethereum.net.message.BlocksMessage;
import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.GetChainMessage;
import org.ethereum.net.message.HelloMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.NotInChainMessage;
import org.ethereum.net.message.PeersMessage;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.message.TransactionsMessage;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class EthereumProtocolHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger("wire");

    Timer chainAskTimer = new Timer();
    int secToAskForChain = 1;

    final Timer timer = new Timer();
    private final static byte[] MAGIC_PREFIX = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91};

    private final static byte[] HELLO_MESSAGE = StaticMessages.HELLO_MESSAGE.getPayload();
    private final static byte[] HELLO_MESSAGE_LEN = ByteUtil.calcPacketLength(HELLO_MESSAGE);

    private long lastPongTime = 0;
    private boolean tearDown = false;

    private PeerListener peerListener;

    public EthereumProtocolHandler() {    }

    public EthereumProtocolHandler(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // TODO: send hello
        // TODO: send ping schedule another ping
        // TODO: ByteBuf vs Stream vs new byte ???
        final ByteBuf buffer = ctx.alloc().buffer(HELLO_MESSAGE.length + 8);

        buffer.writeBytes(MAGIC_PREFIX);
        buffer.writeBytes(HELLO_MESSAGE_LEN);
        buffer.writeBytes(HELLO_MESSAGE);
        logger.info("Send: " + StaticMessages.HELLO_MESSAGE.toString());
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

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                logger.info("[Send: GET_PEERS]");
                sendGetPeers(ctx);
            }
        }, 2000, 60000);

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                logger.info("[Send: GET_TRANSACTIONS]");
                sendGetTransactions(ctx);
            }
        }, 2000, 30000);

        chainAskTimer.schedule(new TimerTask() {

            public void run() {
                logger.info("[Send: GET_CHAIN]");
                sendGetChain(ctx);
            }
        }, 3000, secToAskForChain * 1000);

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
            logger.info(helloMessage.toString());
            if (peerListener != null) peerListener.console(helloMessage.toString());
        }
        // got DISCONNECT
        if (Command.fromInt(command) == DISCONNECT) {
            logger.info("[Recv: DISCONNECT]");
            if (peerListener != null) peerListener.console("[Recv: DISCONNECT]");

            RLPList rlpList = RLP.decode2(payload);
            DisconnectMessage disconnectMessage = new DisconnectMessage(rlpList);

            logger.info(disconnectMessage.toString());
            if (peerListener != null) peerListener.console(disconnectMessage.toString());
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
        // got GETPEERS send peers
        if (Command.fromInt(command) == GET_PEERS) {
            logger.info("[Recv: GETPEERS]" );
            if (peerListener != null) peerListener.console("[Recv: GETPEERS]");

//            String answer = "2240089100000134F9013111F84A8456084B1482765FB84072FD5DBC7F458FB0A52354E25234CEA90A51EA09858A21406056D9B9E0826BB153527E4C4CBEC53B46B0245E6E8503EEABDBF0F1789D7C5C78BBF2B1FDD9090CF84A8455417E2D82765FB840CE73F1F1F1F16C1B3FDA7B18EF7BA3CE17B6F1F1F1F141D3C6C654B7AE88B239407FF1F1F1F119025D785727ED017B6ADD21F1F1F1F1000001E321DBC31824BAF84A8436C91C7582765FB840D592C570B5082D357C30E61E3D8F26317BFD7A3A2A00A36CFB7254FEE80830F26DDFBD6A99712552F3D77314DB4AB58B9989F25699C4997A0F62489D4B86CB4DF84A8436CC0A2982765FB840E34C6E3EAC28CFD3DC930A5AEFD9552FEBCD72C33DFC74D8E4C7CF8A7BA71AE53316ADDBD241EB051ED0871C2B62825E66A45DC6A0E752A7F1C22ABEF9ABDE32";
//            byte[] answerBytes = Hex.decode(answer);

//            ByteBuf buffer = ctx.alloc().buffer(answerBytes.length);
//            buffer.writeBytes(answerBytes);
//            ctx.writeAndFlush(buffer);

            // send getpeers
//            answer = "22 40 08 91 00 00 00 02 C1 10 ";
//            answerBytes = Hex.decode(answer);
//            buffer = ctx.alloc().buffer(answerBytes.length);
            
//            answerBytes = Utils.hexStringToByteArr(answer);
//            buffer = ctx.alloc().buffer(answerBytes.length);
//            buffer.writeBytes(answerBytes);
//            ctx.writeAndFlush(buffer);
        }
        // got PEERS
        if (Command.fromInt(command) == PEERS) {
            logger.info("[Recv: PEERS]");
            if (peerListener != null) peerListener.console("[Recv: PEERS]");

            RLPList rlpList = RLP.decode2(payload);
            PeersMessage peersMessage = new PeersMessage(rlpList);

            MainData.instance.addPeers(peersMessage.getPeers());

            logger.info(peersMessage.toString());
            if (peerListener != null) peerListener.console(peersMessage.toString());
        }
        // got TRANSACTIONS
        if (Command.fromInt(command) == TRANSACTIONS) {
            logger.info("Recv: TRANSACTIONS]");
            if (peerListener != null) peerListener.console("Recv: TRANSACTIONS]");

            RLPList rlpList = RLP.decode2(payload);
            TransactionsMessage transactionsMessage = new TransactionsMessage(rlpList);
            for (Transaction tx :  transactionsMessage.getTransactions())
                MainData.instance.addPendingTransaction(tx);

            // todo: if you got transactions send it to your connected peers
            logger.info(transactionsMessage.toString());
            if (peerListener != null) peerListener.console(transactionsMessage.toString());
        }
        // got BLOCKS
        if (Command.fromInt(command) == BLOCKS) {
            logger.info("[Recv: BLOCKS]");
            if (peerListener != null) peerListener.console("[Recv: BLOCKS]");

            RLPList rlpList = RLP.decode2(payload);

            BlocksMessage blocksMessage = new BlocksMessage(rlpList);
            List<Block> blockList = blocksMessage.getBlockDataList();

            // If we get one block from a peer
            // we ask less swinish
            if (blockList.size() <= 1 && secToAskForChain != 10){

                logger.info("Now we ask for a chain each 10 seconds");
                secToAskForChain = 10;

                chainAskTimer.cancel();
                chainAskTimer.purge();
                chainAskTimer = new Timer();
                chainAskTimer.schedule(new TimerTask() {

                    public void run() {
                        logger.info("[Send: GET_CHAIN]");
                        sendGetChain(ctx);
                    }
                }, 3000, secToAskForChain * 1000);
            }

            // If we get more blocks from a peer
            // we ask more often
            if (blockList.size() > 2 && secToAskForChain != 1){

                logger.info("Now we ask for a chain each 1 seconds");
                secToAskForChain = 11;

                chainAskTimer.cancel();
                chainAskTimer.purge();
                chainAskTimer = new Timer();
                chainAskTimer.schedule(new TimerTask() {

                    public void run() {
                        logger.info("[Send: GET_CHAIN]");
                        sendGetChain(ctx);
                    }
                }, 3000, secToAskForChain * 1000);
            }

            MainData.instance.addBlocks(blockList);
            logger.info(blocksMessage.toString());
            if (peerListener != null) peerListener.console(blocksMessage.toString());
        }
        // got GETCHAIN
        if (Command.fromInt(command) == GET_CHAIN) {
            logger.info("[Recv: GET_CHAIN]");
            if (peerListener != null) peerListener.console("[Recv: GET_CHAIN]");

            RLPList rlpList = RLP.decode2(payload);
            GetChainMessage getChainMessage = new GetChainMessage(rlpList);

            logger.info(getChainMessage.toString());
            if (peerListener != null) peerListener.console(getChainMessage.toString());
        }
        // got NOTINCHAIN
        if (Command.fromInt(command) == NOT_IN_CHAIN) {
            logger.info("[Recv: NOT_IN_CHAIN]");
            if (peerListener != null) peerListener.console("[Recv: NOT_IN_CHAIN]");

            RLPList rlpList = RLP.decode2(payload);
            NotInChainMessage notInChainMessage = new NotInChainMessage(rlpList);

            logger.info(notInChainMessage.toString());
            if (peerListener != null) peerListener.console(notInChainMessage.toString());
        }
        // got GETTRANSACTIONS
        if (Command.fromInt(command) == GET_TRANSACTIONS) {
            logger.info("[Recv: GET_TRANSACTIONS]");
            if (peerListener != null) peerListener.console("[Recv: GET_TRANSACTIONS]");
            // todo: send the queue of the transactions
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
        ctx.close().sync();
        timer.cancel();
    }

    private void sendMsg(Message msg, ChannelHandlerContext ctx){
        byte[] data = msg.getPayload();
        final ByteBuf buffer = ctx.alloc().buffer(data.length + 8);
        byte[] packetLen  = ByteUtil.calcPacketLength(data);

        buffer.writeBytes(MAGIC_PREFIX);
        buffer.writeBytes(packetLen);
        ctx.writeAndFlush(buffer);
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

    private void sendGetPeers(ChannelHandlerContext ctx){
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.GET_PEERS.length);
        buffer.writeBytes(StaticMessages.GET_PEERS);
        ctx.writeAndFlush(buffer);
    }

    private void sendGetTransactions(ChannelHandlerContext ctx){
        ByteBuf buffer = ctx.alloc().buffer(StaticMessages.GET_TRANSACTIONS.length);
        buffer.writeBytes(StaticMessages.GET_TRANSACTIONS);
        ctx.writeAndFlush(buffer);
    }

    private void sendGetChain(ChannelHandlerContext ctx){

        byte[] hash = MainData.instance.getLatestBlockHash();
        GetChainMessage chainMessage = new GetChainMessage((byte)100, hash);

        ByteBuf buffer = ctx.alloc().buffer(chainMessage.getPayload().length + 8);
        buffer.writeBytes(StaticMessages.MAGIC_PACKET);
        buffer.writeBytes(ByteUtil.calcPacketSize(chainMessage.getPayload()));
        buffer.writeBytes(chainMessage.getPayload());

        ctx.writeAndFlush(buffer);
    }

    private void sendTx(ChannelHandlerContext ctx){
        byte[] TX_MSG =
                Hex.decode("2240089100000070F86E12F86B80881BC16D674EC8000094CD2A3D9F938E13CD947EC05ABC7FE734DF8DD8268609184E72A00064801BA0C52C114D4F5A3BA904A9B3036E5E118FE0DBB987FE3955DA20F2CD8F6C21AB9CA06BA4C2874299A55AD947DBC98A25EE895AABF6B625C26C435E84BFD70EDF2F69");
        ByteBuf buffer = ctx.alloc().buffer(TX_MSG.length);
        buffer.writeBytes(TX_MSG);
        ctx.writeAndFlush(buffer);
    }
}