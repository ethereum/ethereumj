package org.ethereum.net.client;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.Command;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.message.*;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.ethereum.net.message.StaticMessages.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class EthereumProtocolHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger("wire");

    private Timer blocksAskTimer = new Timer("ChainAskTimer");
    private final Timer timer = new Timer("MiscMessageTimer");
    
    private int secToAskForBlocks = 1;
    private boolean tearDown = false;
    private PeerListener peerListener;
    private MessageQueue msgQueue = null;

    public EthereumProtocolHandler() {    }

    public EthereumProtocolHandler(PeerListener peerListener) {
        this.peerListener = peerListener;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        msgQueue = new MessageQueue(ctx);

        logger.info("Send: " + StaticMessages.HELLO_MESSAGE.toString());
        msgQueue.sendMessage(StaticMessages.HELLO_MESSAGE);
        sendPing();

        // sample for pinging in background
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {

                if (tearDown) this.cancel();
                sendPing();
            }
        }, 2000, 5000);

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                sendGetPeers();
            }
        }, 2000, 60000);

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                sendGetTransactions();
            }
        }, 2000, 10000);

        blocksAskTimer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                sendGetBlocks();
            }
        }, 1000, secToAskForBlocks * 1000);

    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] payload = (byte[]) msg;

        logger.info("[Recv msg: [{}] ]", Hex.toHexString(payload));

        EthereumListener listener = WorldManager.getInstance().getListener();

        byte commandCode = RLP.getCommandCode(payload);
        Command receivedCommand = Command.fromInt(commandCode);
        
        logger.info("[Recv: {}]", receivedCommand.name());
        if (peerListener != null) peerListener.console("[Recv: " + receivedCommand.name() + "]");
        
        switch(receivedCommand) {
    	
        case HELLO:
            RLPList rlpList = RLP.decode2(payload);
            
            HelloMessage helloMessage = new HelloMessage(rlpList);
            logger.info(helloMessage.toString());
            if (peerListener != null) peerListener.console(helloMessage.toString());

            if (listener != null){
                listener.trace(String.format("Got handshake: [ %s ]", helloMessage.toString()));
                listener.onRecvMessage(helloMessage);
            }
            break;
        case DISCONNECT:
            DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
            msgQueue.receivedMessage(disconnectMessage);

            logger.info(disconnectMessage.toString());
            if (peerListener != null) peerListener.console(disconnectMessage.toString());

            if (listener != null)
                listener.onRecvMessage(disconnectMessage);
            break;
        case PING:
        	msgQueue.receivedMessage(PING_MESSAGE);
            sendPong();

            if (listener != null)
                listener.onRecvMessage(PING_MESSAGE);
            break;
        case PONG:
            msgQueue.receivedMessage(PONG_MESSAGE);

            if (listener != null)
                listener.onRecvMessage(PONG_MESSAGE);
            break;
        case GET_PEERS:
        	msgQueue.receivedMessage(GET_PEERS_MESSAGE);

            // TODO: send peer list

            if (listener != null)
                listener.onRecvMessage(GET_PEERS_MESSAGE);
            break;
        case PEERS:
            PeersMessage peersMessage = new PeersMessage(payload);
            msgQueue.receivedMessage(peersMessage);

            WorldManager.getInstance().addPeers(peersMessage.getPeers());

            logger.info(peersMessage.toString());
            if (peerListener != null) peerListener.console(peersMessage.toString());

            if (listener != null)
                listener.onRecvMessage(peersMessage);
            break;
        case TRANSACTIONS:
        	TransactionsMessage transactionsMessage = new TransactionsMessage(payload);
            msgQueue.receivedMessage(transactionsMessage);

            List<Transaction> txList = transactionsMessage.getTransactions();
            for(Transaction tx : txList)
//				WorldManager.getInstance().getBlockchain()
//						.applyTransaction(null, tx);
                WorldManager.getInstance().getWallet().addTransaction(tx);

            logger.info(transactionsMessage.toString());
            if (peerListener != null) peerListener.console(transactionsMessage.toString());

            if (listener != null)
                listener.onRecvMessage(transactionsMessage);
            break;
        case BLOCKS:
            BlocksMessage blocksMessage = new BlocksMessage(payload);
            List<Block> blockList = blocksMessage.getBlockDataList();
            msgQueue.receivedMessage(blocksMessage);

            // If we get one block from a peer we ask less greedy
            if (blockList.size() <= 1 && secToAskForBlocks != 10) {

                logger.info("Now we ask for blocks each 10 seconds");
                secToAskForBlocks = 10;

                blocksAskTimer.cancel();
                blocksAskTimer.purge();
                blocksAskTimer = new Timer();
                blocksAskTimer.scheduleAtFixedRate(new TimerTask() {

                    public void run() {
                        sendGetBlocks();
                    }
                }, 3000, secToAskForBlocks * 1000);
            }

            // If we get more blocks from a peer we ask more greedy
            if (blockList.size() > 2 && secToAskForBlocks != 1) {

                logger.info("Now we ask for a chain each 1 seconds");
                secToAskForBlocks = 1;

                blocksAskTimer.cancel();
                blocksAskTimer.purge();
                blocksAskTimer = new Timer();
                blocksAskTimer.scheduleAtFixedRate(new TimerTask() {

                    public void run() {
                        sendGetBlocks();
                    }
                }, 3000, secToAskForBlocks * 1000);
            }

            if (blockList.isEmpty()) return;
            WorldManager.getInstance().getBlockchain().getBlockQueue().addBlocks(blockList);
            if (peerListener != null) peerListener.console(blocksMessage.toString());

            if (listener != null)
                listener.onRecvMessage(blocksMessage);
            break;
        case GET_TRANSACTIONS:

            // TODO Implement GET_TRANSACTIONS command
        	
//            List<Transaction> pendingTxList =
//                    WorldManager.getInstance().getBlockchain().getPendingTransactionList();

//            TransactionsMessage txMsg = new TransactionsMessage(pendingTxList);
//            sendMsg(txMsg, ctx);

            if (listener != null)
                listener.onRecvMessage(GET_TRANSACTIONS_MESSAGE);
            break;
        case GET_BLOCK_HASHES:
        	// TODO Implement GET_BLOCK_HASHES command
        	break;
        case BLOCK_HASHES:
        	// TODO Implement BLOCK_HASHES command
        	break;
        case GET_BLOCKS:
        	// TODO Implement GET_BLOCKS command
        	break;
        default:
        	// do nothing and ignore this command
        	break;
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
        timer.cancel();
    }

    public void sendMsg(Message msg) {
        msgQueue.sendMessage(msg);

        EthereumListener listener = WorldManager.getInstance().getListener();
        if (listener != null)
            listener.onSendMessage(msg);
    }

    private void sendPing() {
        sendMsg(PING_MESSAGE);
    }

    private void sendPong() {
        sendMsg(PONG_MESSAGE);
    }

    private void sendGetPeers() {
        sendMsg(GET_PEERS_MESSAGE);
    }

    private void sendGetTransactions() {
        sendMsg(GET_TRANSACTIONS_MESSAGE);
    }

    private void sendGetBlocks() {

        if (WorldManager.getInstance().getBlockchain().getBlockQueue().size() >
                SystemProperties.CONFIG.maxBlocksQueued()) return;

        Block lastBlock = WorldManager.getInstance().getBlockchain().getBlockQueue().getLast();
        if (lastBlock == null) return;

        byte[] hash = lastBlock.getHash();
		GetBlocksMessage getBlocksMessage = new GetBlocksMessage(
				SystemProperties.CONFIG.maxBlocksAsk(), hash);
        sendMsg(getBlocksMessage);
    }

    public void killTimers(){
        blocksAskTimer.cancel();
        blocksAskTimer.purge();

        timer.cancel();
        timer.purge();
   }
}