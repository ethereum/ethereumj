package org.ethereum.net.handler;

import static org.ethereum.net.message.StaticMessages.GET_TRANSACTIONS_MESSAGE;
import static org.ethereum.config.SystemProperties.CONFIG;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.ethereum.core.Block;
import org.ethereum.net.BlockQueue;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.message.BlockHashesMessage;
import org.ethereum.net.message.BlocksMessage;
import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.EthMessage;
import org.ethereum.net.message.GetBlockHashesMessage;
import org.ethereum.net.message.GetBlocksMessage;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.message.StatusMessage;
import org.ethereum.net.message.TransactionsMessage;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
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
@ChannelHandler.Sharable
public class EthHandler extends SimpleChannelInboundHandler<EthMessage> {

	private final static Logger logger = LoggerFactory.getLogger("wire");

	private int secToAskForBlocks = 1;

	private String peerId;
	private Blockchain blockchain;
	private PeerListener peerListener;

	private static String hashRetrievalLock;
	private MessageQueue msgQueue = null;

	private Timer getBlocksTimer 	= new Timer("GetBlocksTimer");
	private Timer getTxTimer	= new Timer("GetTransactionsTimer");

	public EthHandler() {
		this.blockchain = WorldManager.getInstance().getBlockchain();
	}

	public EthHandler(String peerId, PeerListener peerListener) {
		this();
		this.peerId = peerId;
		this.peerListener = peerListener;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		msgQueue = new MessageQueue(ctx, peerListener);
		sendStatus();
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {
		logger.trace("Read channel for {}", ctx.channel().remoteAddress());
		
		switch (msg.getCommand()) {
			case STATUS:
				msgQueue.receivedMessage(msg);
				processStatus((StatusMessage)msg, ctx);
				break;
			case GET_TRANSACTIONS:
				msgQueue.receivedMessage(msg);
				sendPendingTransactions();
				break;
			case TRANSACTIONS:
				msgQueue.receivedMessage(msg);
				// List<Transaction> txList = transactionsMessage.getTransactions();
				// for(Transaction tx : txList)
				// WorldManager.getInstance().getBlockchain().applyTransaction(null,
				// tx);
				// WorldManager.getInstance().getWallet().addTransaction(tx);
				break;
			case GET_BLOCK_HASHES:
				msgQueue.receivedMessage(msg);
//				sendBlockHashes();
				break;
			case BLOCK_HASHES:
				msgQueue.receivedMessage(msg);
				processBlockHashes((BlockHashesMessage)msg);
				break;
			case GET_BLOCKS:
				msgQueue.receivedMessage(msg);
//				sendBlocks();
				break;
			case BLOCKS:
				msgQueue.receivedMessage(msg);
				processBlocks((BlocksMessage)msg);
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
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    	logger.debug("handlerRemoved: kill timers in EthHandler");
    	this.killTimers();
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
		if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH)
				|| msg.getProtocolVersion() != CONFIG.protocolVersion()) {
			logger.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
//			msgQueue.sendMessage(new DisconnectMessage(ReasonCode.INCOMPATIBLE_NETWORK));
			ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
		} else if (msg.getNetworkId() != 0)
			msgQueue.sendMessage(new DisconnectMessage(ReasonCode.INCOMPATIBLE_NETWORK));	
		else {
			BlockQueue chainQueue = this.blockchain.getQueue();
			BigInteger peerTotalDifficulty = new BigInteger(1, msg.getTotalDifficulty());
			BigInteger highestKnownTotalDifficulty = chainQueue.getHighestTotalDifficulty();
			if (highestKnownTotalDifficulty == null 
					|| peerTotalDifficulty.compareTo(highestKnownTotalDifficulty) > 0) {
				hashRetrievalLock = this.peerId;
				chainQueue.setHighestTotalDifficulty(peerTotalDifficulty);
				chainQueue.setBestHash(msg.getBestHash());
				sendGetBlockHashes();
			} else
				startGetBlockTimer();
		}
	}
	
	private void processBlockHashes(BlockHashesMessage blockHashesMessage) {
		List<byte[]> receivedHashes = blockHashesMessage.getBlockHashes();
		BlockQueue chainQueue = this.blockchain.getQueue();

		// result is empty, peer has no more hashes
		// or peer doesn't have the best hash anymore
		if (receivedHashes.isEmpty()
				|| !this.peerId.equals(hashRetrievalLock)) {
			startGetBlockTimer(); // start getting blocks from hash queue
			return;
		}
	
		Iterator<byte[]> hashIterator = receivedHashes.iterator();
		byte[] foundHash, latestHash = this.blockchain.getLatestBlockHash();
		while(hashIterator.hasNext()) {
			foundHash = hashIterator.next();
			if (FastByteComparisons.compareTo(foundHash, 0, 32, latestHash, 0, 32) != 0)
				chainQueue.addHash(foundHash); 	// store unknown hashes in queue until known hash is found
			else {
				// if known hash is found, ignore the rest
				startGetBlockTimer(); // start getting blocks from hash queue
				return;
			}
		}
		// no known hash has been reached
		sendGetBlockHashes(); // another getBlockHashes with last received hash.
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
    	byte protocolVersion = CONFIG.protocolVersion(), networkId = 0;
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
    
	private void sendGetBlockHashes() {
		byte[] bestHash = this.blockchain.getQueue().getBestHash();
		GetBlockHashesMessage msg = new GetBlockHashesMessage(bestHash, CONFIG.maxHashesAsk());
		msgQueue.sendMessage(msg);
	}

	// Parallel download blocks based on hashQueue
    private void sendGetBlocks() {
    	BlockQueue queue = this.blockchain.getQueue();
		if (queue.size() > CONFIG.maxBlocksQueued()) return;
		
        // retrieve list of block hashes from queue
		List<byte[]> hashes = queue.getHashes();

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
    
	private void startTxTimer() {
		getTxTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				sendGetTransactions();
			}
		}, 2000, 10000);
	}

	public void startGetBlockTimer() {
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

	private void stopGetBlocksTimer() {
        getBlocksTimer.cancel();
        getBlocksTimer.purge();
	}

	private void stopGetTxTimer() {
        getTxTimer.cancel();
        getTxTimer.purge();
	}

    public void killTimers(){
    	stopGetBlocksTimer();
    	stopGetTxTimer();
    }
}