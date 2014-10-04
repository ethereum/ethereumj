package org.ethereum.net.handler;

import static org.ethereum.net.message.StaticMessages.GET_TRANSACTIONS_MESSAGE;
import static org.ethereum.config.SystemProperties.CONFIG;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.ethereum.core.Block;
import org.ethereum.core.BlockQueue;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.message.BlockHashesMessage;
import org.ethereum.net.message.BlocksMessage;
import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.GetBlockHashesMessage;
import org.ethereum.net.message.GetBlocksMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.message.StatusMessage;
import org.ethereum.net.message.TransactionsMessage;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process the messages between peers with 'eth' capability on the network.
 * 
 * Peers with 'eth' capability can send/receive:
 * <ul>
 * 	<li>STATUS				:	Announce their status to the peer</li>
 * 	<li>GET_TRANSACTIONS	: 	Request a list of pending transactions</li>
 * 	<li>TRANSACTIONS		:	Send a list of pending transactions</li>
 * 	<li>GET_BLOCK_HASHES	: 	Request a list of known block hashes</li>
 * 	<li>BLOCK_HASHES		:	Send a list of known block hashes</li>
 * 	<li>GET_BLOCKS			: 	Request a list of blocks</li>
 * 	<li>BLOCKS				:	Send a list of blocks</li>
 * </ul>
 */
public class EthHandler extends SimpleChannelInboundHandler<Message> {

	private final static Logger logger = LoggerFactory.getLogger("wire");

	private Timer getBlocksTimer = new Timer("GetBlocksTimer");
	private int secToAskForBlocks = 1;

	private Blockchain blockchain;
	private PeerListener peerListener;
	private EthereumListener listener;

	private MessageQueue msgQueue = null;
	private final Timer timer = new Timer("MiscMessageTimer");

	public EthHandler() {
		this.listener = WorldManager.getInstance().getListener();
		this.blockchain = WorldManager.getInstance().getBlockchain();
	}

	public EthHandler(PeerListener peerListener) {
		this();
		this.peerListener = peerListener;
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, Message msg) throws InterruptedException {
		logger.trace("Read channel for {}", ctx.channel().remoteAddress());
		
		msgQueue.receivedMessage(msg);
		if (listener != null) listener.onRecvMessage(msg);

		switch (msg.getCommand()) {
			case STATUS:
				processStatus((StatusMessage)msg, ctx);
				break;
			case TRANSACTIONS:
				// List<Transaction> txList = transactionsMessage.getTransactions();
				// for(Transaction tx : txList)
				// WorldManager.getInstance().getBlockchain().applyTransaction(null,
				// tx);
				// WorldManager.getInstance().getWallet().addTransaction(tx);
				break;
			case BLOCKS:			
				processBlocks((BlocksMessage)msg);
				break;
			case GET_TRANSACTIONS:
				sendPendingTransactions();
				break;
			case GET_BLOCK_HASHES:
				sendBlockHashes();
				break;
			case BLOCK_HASHES:
				processBlockHashes((BlockHashesMessage)msg);
				break;
			case GET_BLOCKS:
				sendBlocks();
				break;
			default:
				break;
		}
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getCause().toString());
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        msgQueue = new MessageQueue(ctx, peerListener);
        sendStatus();
    }

	/**
     * Processing:
     * <ul>
     *   <li>checking if peer is using the same genesis, protocol and network</li>
     *   <li>seeing if total difficulty is higher than total difficulty from all other peers</li>
     * 	 <li>send GET_BLOCK_HASHES to this peer based on bestHash</li>
     * </ul>
     * 
     * @param msg is the StatusMessage
     * @param ctx the ChannelHandlerContext
     */
	private void processStatus(StatusMessage msg, ChannelHandlerContext ctx) {
		if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH) || msg.getProtocolVersion() != 33)
			ctx.pipeline().remove(this);
		else if (msg.getNetworkId() != 0)
			msgQueue.sendMessage(new DisconnectMessage(ReasonCode.INCOMPATIBLE_NETWORK));	
		else {
			BlockQueue chainQueue = this.blockchain.getQueue();
			BigInteger peerTotalDifficulty = new BigInteger(1, msg.getTotalDifficulty());
			BigInteger highestKnownTotalDifficulty = chainQueue.getHighestTotalDifficulty();
//			if (peerTotalDifficulty.compareTo(highestKnownTotalDifficulty) > 0) {
//				this.blockchain.getQueue().setHighestTotalDifficulty(peerTotalDifficulty);
//				this.blockchain.getQueue().setBestHash(msg.getBestHash());
//				sendGetBlockHashes(msg.getBestHash());
//			}
			startTimers();
		}
	}
	
	private void processBlockHashes(BlockHashesMessage blockHashesMessage) {
    	// for each block hash
    	//		check if blockHash != known hash
    	// 		store blockhash
    	// if no known hash has been reached, another getBlockHashes with last stored hash.
	}
	
	private void processBlocks(BlocksMessage blocksMessage) {
		List<Block> blockList = blocksMessage.getBlocks();
        // If we get one block from a peer we ask less greedy
        if (blockList.size() <= 1 && secToAskForBlocks != 10) {
        	logger.info("Now we ask for blocks every 10 seconds");
            updateGetBlocksTimer(10);
        }

        // If we get more blocks from a peer we ask more greedy
        if (blockList.size() > 2 && secToAskForBlocks != 1) {
        	logger.info("Now we ask for a chain every 1 seconds");
            updateGetBlocksTimer(1);
        }

        if (blockList.isEmpty()) return;
        this.blockchain.getQueue().addBlocks(blockList);
		
	}
	
    private void sendStatus() {
    	byte protocolVersion = 33, networkId = 0;
    	BigInteger totalDifficulty = this.blockchain.getTotalDifficulty();
		byte[] bestHash = this.blockchain.getLatestBlockHash();
		StatusMessage msg = new StatusMessage(protocolVersion, networkId,
				ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, Blockchain.GENESIS_HASH);
		msgQueue.sendMessage(msg);
    }

    /*
     * The wire gets data for signed transactions and
     * sends it to the net.
     */
    public void sendTransaction(Transaction transaction) {
        Set<Transaction> txs = new HashSet<>(Arrays.asList(transaction));
        TransactionsMessage msg = new TransactionsMessage(txs);
        msgQueue.sendMessage(msg);
    }
    
    private void sendGetTransactions() {
    	msgQueue.sendMessage(GET_TRANSACTIONS_MESSAGE);
    }
    
	private void sendGetBlockHashes(byte[] bestHash) {
		GetBlockHashesMessage msg = new GetBlockHashesMessage(bestHash, CONFIG.maxHashesAsk());
		msgQueue.sendMessage(msg);
	}

    private void sendGetBlocks() {

		if (WorldManager.getInstance().getBlockchain().getQueue().size() > 
				CONFIG.maxBlocksQueued()) return;

		Block lastBlock = this.blockchain.getQueue().getLastBlock();
        if (lastBlock == null) return;

        // retrieve list of block hashes from queue
        int blocksPerPeer = CONFIG.maxBlocksAsk();
		List<byte[]> hashes = this.blockchain.getQueue().getHashes(blocksPerPeer);

        GetBlocksMessage msg = new GetBlocksMessage(hashes);
        msgQueue.sendMessage(msg);
    }
    
	private void sendPendingTransactions() {
    	Set<Transaction> pendingTxs =
                WorldManager.getInstance().getPendingTransactions();
        TransactionsMessage msg = new TransactionsMessage(pendingTxs);
        msgQueue.sendMessage(msg);
	}
    
    private void sendBlocks() {
    	// TODO: Send blocks
	}

	private void sendBlockHashes() {
		// TODO: Send block hashes
	}
    
    private void startTimers() {
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				sendGetTransactions();
			}
		}, 2000, 10000);
	        
		getBlocksTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				sendGetBlocks();
			}
		}, 1000, secToAskForBlocks * 1000);
    }
    
	private void updateGetBlocksTimer(int seconds) {
        secToAskForBlocks = seconds;
        getBlocksTimer.cancel();
        getBlocksTimer.purge();
        getBlocksTimer = new Timer();
        getBlocksTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetBlocks();
            }
        }, 3000, secToAskForBlocks * 1000);
	}
	
    public void killTimers(){
        getBlocksTimer.cancel();
        getBlocksTimer.purge();

        timer.cancel();
        timer.purge();
    }
}