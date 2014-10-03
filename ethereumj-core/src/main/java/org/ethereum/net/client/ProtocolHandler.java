package org.ethereum.net.client;

import static org.ethereum.net.message.StaticMessages.GET_TRANSACTIONS_MESSAGE;
import static org.ethereum.net.message.StaticMessages.PING_MESSAGE;
import static org.ethereum.net.message.StaticMessages.PONG_MESSAGE;
import static org.ethereum.net.message.StaticMessages.HELLO_MESSAGE;
import static org.ethereum.net.message.StaticMessages.GET_PEERS_MESSAGE;
import static org.ethereum.config.SystemProperties.CONFIG;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.PeerListener;
import org.ethereum.net.message.BlockHashesMessage;
import org.ethereum.net.message.BlocksMessage;
import org.ethereum.net.message.Command;
import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.GetBlockHashesMessage;
import org.ethereum.net.message.GetBlocksMessage;
import org.ethereum.net.message.HelloMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.PeersMessage;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.message.StatusMessage;
import org.ethereum.net.message.TransactionsMessage;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process the basic messages between every peer on the network.
 * 
 * Peers can send/receive
 * <ul>
 * 	<li>HELLO		:	Announce themselves to the network</li>
 * 	<li>DISCONNECT: 	Disconnect themselves from the network</li>
 * 	<li>GET_PEERS	: 	Request a list of other knows peers</li>
 * 	<li>PEERS		:	Send a list of known peers</li>
 * 	<li>PING		: 	Check if another peer is still alive</li>
 * 	<li>PONG		:	Confirm that they themselves are still alive</li>
 * </ul>
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
public class ProtocolHandler extends ChannelInboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger("wire");

	private long lastPongTime;
	private boolean tearDown = false;
	private HelloMessage handshake = null;

	private Timer blocksAskTimer = new Timer("ChainAskTimer");
	private int secToAskForBlocks = 1;

	private PeerDiscovery peerDiscovery;
	private PeerListener peerListener;
	private EthereumListener listener;

	private MessageQueue msgQueue = null;
	private final Timer timer = new Timer("MiscMessageTimer");

	public ProtocolHandler() {
		this.listener = WorldManager.getInstance().getListener();
		this.peerDiscovery = WorldManager.getInstance().getPeerDiscovery();
	}

	public ProtocolHandler(PeerListener peerListener) {
		this();
		this.peerListener = peerListener;
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		msgQueue = new MessageQueue(ctx, peerListener);
		sendHello();
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws InterruptedException {

		byte[] payload = (byte[]) msg;
		Command receivedCommand = Command.fromInt(RLP.getCommandCode(payload));

		switch (receivedCommand) {
			case HELLO:
				HelloMessage helloMessage = new HelloMessage(payload);
				msgQueue.receivedMessage(helloMessage);
			
				setHandshake(helloMessage, ctx);
			
				if (listener != null) listener.onRecvMessage(helloMessage);
				break;
			case STATUS:
				StatusMessage statusMessage = new StatusMessage(payload);
				msgQueue.receivedMessage(statusMessage);
			
				processStatus(statusMessage);

				if (listener != null) listener.onRecvMessage(statusMessage);
				break;
			case DISCONNECT:
				DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
				msgQueue.receivedMessage(disconnectMessage);
			
				if (listener != null) listener.onRecvMessage(disconnectMessage);
				break;
			case PING:
				msgQueue.receivedMessage(PING_MESSAGE);
			
				sendPong();
			
				if (listener != null) listener.onRecvMessage(PING_MESSAGE);
				break;
			case PONG:
				msgQueue.receivedMessage(PONG_MESSAGE);
			
				lastPongTime = System.currentTimeMillis();
			
				if (listener != null) listener.onRecvMessage(PONG_MESSAGE);
				break;
			case GET_PEERS:
				msgQueue.receivedMessage(GET_PEERS_MESSAGE);
			
				sendPeers();
			
				if (listener != null) listener.onRecvMessage(GET_PEERS_MESSAGE);
				break;
			case PEERS:
				PeersMessage peersMessage = new PeersMessage(payload);
				msgQueue.receivedMessage(peersMessage);
			
				processPeers(peersMessage);
			
				if (listener != null) listener.onRecvMessage(peersMessage);
				break;
			case TRANSACTIONS:
				TransactionsMessage transactionsMessage = new TransactionsMessage(payload);
				msgQueue.receivedMessage(transactionsMessage);
			
				// List<Transaction> txList = transactionsMessage.getTransactions();
				// for(Transaction tx : txList)
				// WorldManager.getInstance().getBlockchain().applyTransaction(null,
				// tx);
				// WorldManager.getInstance().getWallet().addTransaction(tx);
			
				if (listener != null) listener.onRecvMessage(transactionsMessage);
				break;
			case BLOCKS:
				BlocksMessage blocksMessage = new BlocksMessage(payload);
				msgQueue.receivedMessage(blocksMessage);
			
				processBlocks(blocksMessage);
			
				if (listener != null) listener.onRecvMessage(blocksMessage);
				break;
			case GET_TRANSACTIONS:
				msgQueue.receivedMessage(GET_TRANSACTIONS_MESSAGE);
			
				sendPendingTransactions();
			
				if (listener != null) listener.onRecvMessage(GET_TRANSACTIONS_MESSAGE);
				break;
			case GET_BLOCK_HASHES:
				GetBlockHashesMessage getBlockHashesMessage = new GetBlockHashesMessage(payload);
				msgQueue.receivedMessage(getBlockHashesMessage);
			
				sendBlockHashes();
			
				if (listener != null) listener.onRecvMessage(getBlockHashesMessage);
				break;
			case BLOCK_HASHES:
				BlockHashesMessage blockHashesMessage = new BlockHashesMessage(payload);
				msgQueue.receivedMessage(blockHashesMessage);
			
				processBlockHashes(blockHashesMessage);
			
				if (listener != null) listener.onRecvMessage(blockHashesMessage);
				break;
			case GET_BLOCKS:
				GetBlocksMessage getBlocksMessage = new GetBlocksMessage(payload);
				msgQueue.receivedMessage(getBlocksMessage);
			
				sendBlocks();
			
				if (listener != null) listener.onRecvMessage(getBlocksMessage);
				break;
			default:
				break;
		}
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // limit the size of recieving buffer to 1024
        ctx.channel().config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        ctx.channel().config().setOption(ChannelOption.SO_RCVBUF, 32368);
    }

    private void processPeers(PeersMessage peersMessage) {
    	peerDiscovery.addPeers(peersMessage.getPeers());
	}

	/**
     * Processing status from peers means
     * - checking if peer is using the same genesis, protocol and network
     * - seeing if total difficulty is higher than total difficulty from all other peers
     * - send a GET_BLOCK_HASHES based on bestHash
     * @param msg is the StatusMessage
     */
	private void processStatus(StatusMessage msg) {
		if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH))
			sendDisconnect(ReasonCode.WRONG_GENESIS);
		else if (msg.getProtocolVersion() != 33)
			sendDisconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);		
		else if (msg.getNetworkId() != 0)
			sendDisconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);	
		else {
//			BlockQueue chainQueue = WorldManager.getInstance().getBlockchain().getQueue();
//			BigInteger peerTotalDifficulty = new BigInteger(1, msg.getTotalDifficulty());
//			BigInteger highestKnownTotalDifficulty = chainQueue.getHighestTotalDifficulty();
//			if (peerTotalDifficulty.compareTo(highestKnownTotalDifficulty) > 0) {
//				WorldManager.getInstance().getBlockchain().getQueue()
//						.setHighestTotalDifficulty(peerTotalDifficulty);
//				WorldManager.getInstance().getBlockchain().getQueue()
//						.setBestHash(msg.getBestHash());
//				sendGetBlockHashes(msg.getBestHash());
//			}
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
            updateBlockAskTimer(10);
        }

        // If we get more blocks from a peer we ask more greedy
        if (blockList.size() > 2 && secToAskForBlocks != 1) {
        	logger.info("Now we ask for a chain every 1 seconds");
            updateBlockAskTimer(1);
        }

        if (blockList.isEmpty()) return;
        WorldManager.getInstance().getBlockchain().getQueue().addBlocks(blockList);
		
	}
	
	private void sendMsg(Message msg) {
        msgQueue.sendMessage(msg);

        if (listener != null) listener.onSendMessage(msg);
    }
    
    private void sendHello() {
        sendMsg(HELLO_MESSAGE);
    }
    
    private void sendDisconnect(ReasonCode reason) {
    	DisconnectMessage msg = new DisconnectMessage(reason);
    	sendMsg(msg);
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
    
    private void sendPeers() {
    	PeersMessage msg = new PeersMessage(peerDiscovery.getPeers());
        sendMsg(msg);
    }

    private void sendStatus() {
    	Blockchain chain = WorldManager.getInstance().getBlockchain();
    	byte protocolVersion = 33, networkId = 0;
    	BigInteger totalDifficulty = chain.getTotalDifficulty();
		byte[] bestHash = chain.getLatestBlockHash();
		StatusMessage msg = new StatusMessage(protocolVersion, networkId,
				ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, Blockchain.GENESIS_HASH);
        sendMsg(msg);
    }

    /*
     * The wire gets data for signed transactions and
     * sends it to the net.
     */
    public void sendTransaction(Transaction transaction) {
        Set<Transaction> txs = new HashSet<>(Arrays.asList(transaction));
        TransactionsMessage msg = new TransactionsMessage(txs);
        sendMsg(msg);
    }
    
    private void sendGetTransactions() {
        sendMsg(GET_TRANSACTIONS_MESSAGE);
    }
    
	private void sendGetBlockHashes(byte[] bestHash) {
		GetBlockHashesMessage msg = new GetBlockHashesMessage(bestHash, CONFIG.maxHashesAsk());
		sendMsg(msg);
	}

    private void sendGetBlocks() {

		if (WorldManager.getInstance().getBlockchain().getQueue().size() > 
				CONFIG.maxBlocksQueued()) return;

		Block lastBlock = WorldManager.getInstance().getBlockchain().getQueue()
				.getLastBlock();
        if (lastBlock == null) return;

        // retrieve list of block hashes from queue
        int blocksPerPeer = CONFIG.maxBlocksAsk();
		List<byte[]> hashes = WorldManager.getInstance().getBlockchain()
				.getQueue().getHashes(blocksPerPeer);

        GetBlocksMessage msg = new GetBlocksMessage(hashes);
        sendMsg(msg);
    }
    
	private void sendPendingTransactions() {
    	Set<Transaction> pendingTxs =
                WorldManager.getInstance().getPendingTransactions();
        TransactionsMessage msg = new TransactionsMessage(pendingTxs);
        sendMsg(msg);
	}
    
    private void sendBlocks() {
    	// TODO: Send blocks
	}

	private void sendBlockHashes() {
		// TODO: Send block hashes
	}
    
    private void setHandshake(HelloMessage msg, ChannelHandlerContext ctx) {
    	// TODO validate p2pVersion
    	handshake = msg;
    	InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
    	int port = msg.getListenPort();
    	byte[] peerId = msg.getPeerId();
    	Peer confirmedPeer = new Peer(address, port, peerId);
    	confirmedPeer.setOnline(true);
    	confirmedPeer.setHandshake(handshake);
    	WorldManager.getInstance().getPeerDiscovery().getPeers().add(confirmedPeer);
    	
    	startTimers();
    }
    
    private void startTimers() {
        // sample for pinging in background
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (tearDown) cancel();
                sendPing();
            }
        }, 2000, 5000);

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetPeers();
            }
        }, 2000, 60000);
    	
    	for (String capability : handshake.getCapabilities()) {
			if("eth".equals(capability)) {
		    	sendStatus();
//				timer.scheduleAtFixedRate(new TimerTask() {
//					public void run() {
//						sendGetTransactions();
//					}
//				}, 2000, 10000);
	        
//				blocksAskTimer.scheduleAtFixedRate(new TimerTask() {
//					public void run() {
//						sendGetBlocks();
//					}
//				}, 1000, secToAskForBlocks * 1000);
			}
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
	
    public void killTimers(){
        blocksAskTimer.cancel();
        blocksAskTimer.purge();

        timer.cancel();
        timer.purge();
    }
    
    protected HelloMessage getHandshake() {
        return handshake;
    }
}