package org.ethereum.net.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.Command;
import org.ethereum.net.PeerListener;
import org.ethereum.net.message.*;
import org.ethereum.net.peerdiscovery.PeerProtocolHandler;
import org.ethereum.util.RLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.ethereum.net.message.StaticMessages.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 10/04/14 08:19
 */
public class EthereumProtocolHandler extends PeerProtocolHandler {

    private Logger logger = LoggerFactory.getLogger("wire");

    private Timer blocksAskTimer = new Timer("ChainAskTimer");
    
    private int secToAskForBlocks = 1;
    private boolean tearDown = false;

    public EthereumProtocolHandler() {    }

    public EthereumProtocolHandler(PeerListener peerListener) {
        super(peerListener);
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

    	super.channelActive(ctx);
    	sendGetBlockHashes();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetTransactions();
            }
        }, 2000, 10000);

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetBlockHashes();
            }
        }, 2000, 10000);
        
        blocksAskTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetBlocks();
            }
        }, 1000, secToAskForBlocks * 1000);

    }

	@Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws InterruptedException {
    	super.channelRead(ctx, msg);
        byte[] payload = (byte[]) msg;

        EthereumListener listener = WorldManager.getInstance().getListener();
        Command receivedCommand = Command.fromInt(RLP.getCommandCode(payload));
        if (peerListener != null) peerListener.console("[Recv: " + receivedCommand.name() + "]");
        
        switch(receivedCommand) {
        	case HELLO:
        		sendStatus();
        		break;
        	case TRANSACTIONS:
        		TransactionsMessage transactionsMessage = new TransactionsMessage(payload);
        		msgQueue.receivedMessage(transactionsMessage);
        		if (peerListener != null) peerListener.console(transactionsMessage.toString());
	
        		List<Transaction> txList = transactionsMessage.getTransactions();
        		for(Transaction tx : txList)
//					WorldManager.getInstance().getBlockchain().applyTransaction(null, tx);
	                WorldManager.getInstance().getWallet().addTransaction(tx);
	
	            if (listener != null)
	                listener.onRecvMessage(transactionsMessage);
	            break;
	        case BLOCKS:
	            BlocksMessage blocksMessage = new BlocksMessage(payload);
	            List<Block> blockList = blocksMessage.getBlockDataList();
	            msgQueue.receivedMessage(blocksMessage);
	            if (peerListener != null) peerListener.console(blocksMessage.toString());
	
	            // If we get one block from a peer we ask less greedy
	            if (blockList.size() <= 1 && secToAskForBlocks != 10) {
	            	logger.info("Now we ask for blocks every 10 seconds");
	                updateBlockAskTimer(10);
	            }
	
	            // If we get more blocks from a peer we ask more greedy
	            if (blockList.size() > 2 && secToAskForBlocks != 1) {
	            	logger.info("Now we ask for a chain every 1 seconds");
	                updateBlockAskTimer(1);
	            }
	
	            if (blockList.isEmpty()) return;
	            WorldManager.getInstance().getBlockchain().getBlockQueue().addBlocks(blockList);
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
	            GetBlockHashesMessage getBlockHashesMessage = new GetBlockHashesMessage(payload);
	            msgQueue.receivedMessage(getBlockHashesMessage);
	            if (peerListener != null) peerListener.console(getBlockHashesMessage.toString());

	            sendBlockHashes();
	            
	            if (listener != null)
	                listener.onRecvMessage(getBlockHashesMessage);
	            break;
	        case BLOCK_HASHES:
	        	BlockHashesMessage blockHashesMessage = new BlockHashesMessage(payload);
	        	msgQueue.receivedMessage(blockHashesMessage);
	            if (peerListener != null) peerListener.console(blockHashesMessage.toString());

	        	// TODO Process Block Hashes
	            if (listener != null)
	                listener.onRecvMessage(blockHashesMessage);
	            break;
	        case GET_BLOCKS:
	        	GetBlocksMessage getBlocksMessage = new GetBlocksMessage(payload);
	        	msgQueue.receivedMessage(getBlocksMessage);

	        	sendBlocks();

	        	if (listener != null)
	            	listener.onRecvMessage(getBlocksMessage);
	            break;
	        default:
	        	// do nothing and ignore this command
	        	break;
        }
    }

	private void updateBlockAskTimer(int seconds) {
        secToAskForBlocks = seconds;
        blocksAskTimer.cancel();
        blocksAskTimer.purge();
        blocksAskTimer = new Timer();
        blocksAskTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetBlocks();
            }
        }, 3000, secToAskForBlocks * 1000);
	}

	@Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // limit the size of recieving buffer to 1024
        ctx.channel().config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        ctx.channel().config().setOption(ChannelOption.SO_RCVBUF, 32368);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws InterruptedException  {
        this.tearDown = true;
        logger.info("Lost connection to the server");
        logger.error(cause.getMessage(), cause);
        ctx.close().sync();
        timer.cancel();
    }
    
    protected void sendStatus() {
    	byte protocolVersion = 0, networkId = 0;
    	byte[] totalDifficulty = WorldManager.getInstance().getBlockchain().getTotalDifficulty();
    	byte[] bestHash = WorldManager.getInstance().getBlockchain().getLatestBlockHash();
    	byte[] genesisHash = StaticMessages.GENESIS_HASH;
    	StatusMessage peersMessage = new StatusMessage(protocolVersion, networkId, 
    			totalDifficulty, bestHash, genesisHash);
        sendMsg(peersMessage);
    }

    private void sendGetTransactions() {
        sendMsg(GET_TRANSACTIONS_MESSAGE);
    }
    
    private void sendGetBlockHashes() {
    	byte[] lastHash = WorldManager.getInstance().getBlockchain().getLatestBlockHash();
		GetBlockHashesMessage getBlockHashesMessage = new GetBlockHashesMessage(lastHash, 128);
		sendMsg(getBlockHashesMessage);
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
    
    private void sendBlocks() {
    	// TODO: Send blocks
	}

	private void sendBlockHashes() {
		// TODO: Send block hashes
	}

    public void killTimers(){
        blocksAskTimer.cancel();
        blocksAskTimer.purge();

        timer.cancel();
        timer.purge();
   }
}