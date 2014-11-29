package org.ethereum.net.eth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.message.StaticMessages.GET_TRANSACTIONS_MESSAGE;

/**
 * Process the messages between peers with 'eth' capability on the network.
 * <p>
 * Peers with 'eth' capability can send/receive:
 * <ul>
 * <li>STATUS				:	Announce their status to the peer</li>
 * <li>GET_TRANSACTIONS   	: 	Request a list of pending transactions</li>
 * <li>TRANSACTIONS		    :	Send a list of pending transactions</li>
 * <li>GET_BLOCK_HASHES	    : 	Request a list of known block hashes</li>
 * <li>BLOCK_HASHES		    :	Send a list of known block hashes</li>
 * <li>GET_BLOCKS			: 	Request a list of blocks</li>
 * <li>BLOCKS				:	Send a list of blocks</li>
 * </ul>
 */
@Component
@Scope("prototype")
public class EthHandler extends SimpleChannelInboundHandler<EthMessage> {

    public final static byte VERSION = 45;
    public final static byte NETWORK_ID = 0x0;

    private final static Logger logger = LoggerFactory.getLogger("net");

    private String peerId;

    private static String hashRetrievalLock;

    private MessageQueue msgQueue = null;

    private SyncSatus syncStatus = SyncSatus.INIT;
    private boolean active = false;
    private StatusMessage handshakeStatusMessage = null;

    private boolean peerDiscoveryMode = false;

    private Timer getBlocksTimer = new Timer("GetBlocksTimer");
    private Timer getTxTimer = new Timer("GetTransactionsTimer");

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private WorldManager worldManager;

    public EthHandler(){
        this.peerDiscoveryMode = false;
    }

    public EthHandler(MessageQueue msgQueue, boolean peerDiscoveryMode) {
    	this.peerDiscoveryMode = peerDiscoveryMode;
        this.msgQueue = msgQueue;
    }

    public void activate(){
        logger.info("ETH protocol activated");
        worldManager.getListener().trace("ETH protocol activated");

        active = true;
        sendStatus();
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }


    public boolean isActive(){
        return active;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (EthMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("EthHandler invoke: [{}]", msg.getCommand());

        worldManager.getListener().trace(String.format("EthHandler invoke: [%s]", msg.getCommand()));

        switch (msg.getCommand()) {
            case STATUS:
                msgQueue.receivedMessage(msg);
				processStatus((StatusMessage) msg, ctx);
                break;
            case GET_TRANSACTIONS:
                // todo: eventually get_transaction is going deprecated
//                msgQueue.receivedMessage(msg);
//                sendPendingTransactions();
                break;
            case TRANSACTIONS:
                msgQueue.receivedMessage(msg);
                processTransactions((TransactionsMessage)msg);
                // List<Transaction> txList = transactionsMessage.getTransactions();
                // for(Transaction tx : txList)
                // WorldManager.getInstance().getBlockchain().applyTransaction(null,
                // tx);
                // WorldManager.getInstance().getWallet().addTransaction(tx);
                break;
            case GET_BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                processGetBlockHashes((GetBlockHashesMessage) msg);
                break;
            case BLOCK_HASHES:
                msgQueue.receivedMessage(msg);
                processBlockHashes((BlockHashesMessage) msg);
                break;
            case GET_BLOCKS:
                msgQueue.receivedMessage(msg);
                processGetBlocks( (GetBlocksMessage) msg  );
                break;
            case BLOCKS:
                msgQueue.receivedMessage(msg);
                processBlocks((BlocksMessage) msg);
                break;
            case NEW_BLOCK:
                msgQueue.receivedMessage(msg);
                procesNewBlock((NewBlockMessage)msg);
            default:
                break;
        }
    }

    private void processTransactions(TransactionsMessage msg) {

        Set<Transaction> txSet = msg.getTransactions();
        worldManager.addPendingTransactions(txSet);

        for (Transaction tx : txSet){
            worldManager.getWallet().addTransaction(tx);
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
        active = false;
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
    public void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {

        this.handshakeStatusMessage = msg;
        if (peerDiscoveryMode) {
            msgQueue.sendMessage(new DisconnectMessage(ReasonCode.REQUESTED));
            killTimers();
            ctx.close().sync();
            ctx.disconnect().sync();
            return;
        }

        if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH)
                || msg.getProtocolVersion() != EthHandler.VERSION) {
            logger.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
//			msgQueue.sendMessage(new DisconnectMessage(ReasonCode.INCOMPATIBLE_NETWORK));
            ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
        } else if (msg.getNetworkId() != EthHandler.NETWORK_ID)
            msgQueue.sendMessage(new DisconnectMessage(ReasonCode.INCOMPATIBLE_NETWORK));
        else {
            BlockQueue chainQueue = blockchain.getQueue();
            BigInteger peerTotalDifficulty = new BigInteger(1, msg.getTotalDifficulty());
            BigInteger highestKnownTotalDifficulty = blockchain.getTotalDifficulty();
            if ( highestKnownTotalDifficulty == null ||
                 peerTotalDifficulty.compareTo(highestKnownTotalDifficulty) > 0) {

                logger.info(" Their chain is better: total difficulty : {} vs {}",
                        peerTotalDifficulty.toString(),
                        highestKnownTotalDifficulty == null ? "0" : highestKnownTotalDifficulty.toString());

                hashRetrievalLock = this.peerId;
                chainQueue.setHighestTotalDifficulty(peerTotalDifficulty);
                chainQueue.setBestHash(msg.getBestHash());
                syncStatus = SyncSatus.HASH_RETRIEVING;
                sendGetBlockHashes();
            } else{
                logger.info("The peer sync process fully complete");
                syncStatus = SyncSatus.SYNC_DONE;
            }
        }
    }

    private void processBlockHashes(BlockHashesMessage blockHashesMessage) {

        List<byte[]> receivedHashes = blockHashesMessage.getBlockHashes();
        BlockQueue chainQueue = blockchain.getQueue();

        // result is empty, peer has no more hashes
        // or peer doesn't have the best hash anymore
        if (receivedHashes.isEmpty()
                || !this.peerId.equals(hashRetrievalLock)) {
            startGetBlockTimer(); // start getting blocks from hash queue
            return;
        }

        Iterator<byte[]> hashIterator = receivedHashes.iterator();
        byte[] foundHash, latestHash = blockchain.getBestBlockHash();
        while (hashIterator.hasNext()) {
            foundHash = hashIterator.next();
            if (FastByteComparisons.compareTo(foundHash, 0, 32, latestHash, 0, 32) != 0){
                chainQueue.addHash(foundHash);    // store unknown hashes in queue until known hash is found
            }
            else {

                logger.trace("Catch up with the hashes until: {[]}", foundHash);
                // if known hash is found, ignore the rest
                startGetBlockTimer(); // start getting blocks from hash queue
                return;
            }
        }
        // no known hash has been reached
        chainQueue.logHashQueueSize();
        sendGetBlockHashes(); // another getBlockHashes with last received hash.
    }

    private void processBlocks(BlocksMessage blocksMessage) {

        List<Block> blockList = blocksMessage.getBlocks();

        if (blockList.isEmpty()) return;
        blockchain.getQueue().addBlocks(blockList);
        blockchain.getQueue().logHashQueueSize();

        // If we got less blocks then we could get,
        // it the correct indication that we are in sync we
        // the chain from here there will be NEW_BLOCK only
        // message expectation
        if (blockList.size() < CONFIG.maxBlocksAsk()) {
            logger.info(" The peer sync process fully complete");
            syncStatus = SyncSatus.SYNC_DONE;
            stopGetBlocksTimer();
        }
    }


    /**
     * Processing NEW_BLOCK announce message
     * @param newBlockMessage - new block message
     */
    public void procesNewBlock(NewBlockMessage newBlockMessage){

        Block newBlock = newBlockMessage.getBlock();

        // If the hashes still being downloaded ignore the NEW_BLOCKs
        // that block hash will be retrieved by the others and letter the block itself
        if (syncStatus == SyncSatus.INIT || syncStatus == SyncSatus.HASH_RETRIEVING) {
            logger.debug("Sync status INIT or HASH_RETREIVING ignore new block.index: [{}]", newBlock.getNumber());
            return;
        }

        // If the GET_BLOCKs stage started add hash to the end of the hash list
        // then the block will be retrieved in it's turn;
        if (syncStatus == SyncSatus.BLOCK_RETRIEVING){
            logger.debug("Sync status BLOCK_RETREIVING add to the end of hash list: block.index: [{}]",
                    newBlock.getNumber());
            blockchain.getQueue().addNewBlockHash(newBlock.getHash());
            return;
        }

        // here is post sync process
        logger.info("New block received: block.index [{}]", newBlock.getNumber());

/*
        if (blockchain.hasParentOnTheChain(newBlock) && gap <=0){
            //todo: here we create an alternative chain.
            return;
        }

        if (!blockchain.hasParentOnTheChain(newBlock)){
            //todo: here we check if one of alt chains is connecting this guy
            return;
        }

        if (blockchain.hasParentOnTheChain(newBlock) && gap > 1){
            logger.error("Gap in the chain, go out of sync");
            this.syncStatus = SyncSatus.HASH_RETRIEVING;
            blockchain.getQueue().addHash(newBlock.getHash());
            sendGetBlockHashes();
            return;
        }
*/

        // adding block to the queue
        // there will be decided how to
        // connect it to the chain
        blockchain.getQueue().addBlock(newBlockMessage.getBlock());
        blockchain.getQueue().logHashQueueSize();

    }

    private void sendStatus(){
        byte protocolVersion = EthHandler.VERSION, networkId = EthHandler.NETWORK_ID;
        BigInteger totalDifficulty = blockchain.getTotalDifficulty();
        byte[] bestHash = blockchain.getBestBlockHash();
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
        byte[] bestHash = blockchain.getQueue().getBestHash();
        GetBlockHashesMessage msg = new GetBlockHashesMessage(bestHash, CONFIG.maxHashesAsk());
        msgQueue.sendMessage(msg);
    }

    // Parallel download blocks based on hashQueue
    private void sendGetBlocks() {
        BlockQueue queue = blockchain.getQueue();
        if (queue.size() > CONFIG.maxBlocksQueued()) return;

        // retrieve list of block hashes from queue
        List<byte[]> hashes = queue.getHashes();
        if (hashes.isEmpty()) {
            stopGetBlocksTimer();
            return;
        }

        GetBlocksMessage msg = new GetBlocksMessage(hashes);
        msgQueue.sendMessage(msg);
    }

    private void sendPendingTransactions() {
        Set<Transaction> pendingTxs =
                worldManager.getPendingTransactions();
        TransactionsMessage msg = new TransactionsMessage(pendingTxs);
        msgQueue.sendMessage(msg);
    }

    private void processGetBlockHashes(GetBlockHashesMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFrom(msg.getBestHash(), msg.getMaxBlocks());

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        msgQueue.sendMessage(msgHashes);
    }

    private void processGetBlocks(GetBlocksMessage msg) {

        List<byte[]> hashes = msg.getBlockHashes();

        Vector<Block> blocks = new Vector<>();
        for (byte[] hash : hashes){
            Block block =  blockchain.getBlockByHash(hash);
            blocks.add(block);
        }

        BlocksMessage bm = new BlocksMessage(blocks);
        msgQueue.sendMessage(bm);
    }


    private void startTxTimer() {
        getTxTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                sendGetTransactions();
            }
        }, 2000, 10000);
    }

    public void startGetBlockTimer() {
        syncStatus = SyncSatus.BLOCK_RETRIEVING;
        getBlocksTimer = new Timer("GetBlocksTimer");
        getBlocksTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                BlockQueue blockQueue = blockchain.getQueue();
                if (blockQueue.size() > CONFIG.maxBlocksQueued()) {
                    logger.trace("Blocks queue too big temporary postpone blocks request");
                    return;
                }
                sendGetBlocks();
            }
        }, 300, 10);
    }

    private void stopGetBlocksTimer() {
        getBlocksTimer.cancel();
        getBlocksTimer.purge();
    }

    private void stopGetTxTimer() {
        getTxTimer.cancel();
        getTxTimer.purge();
    }

    public void killTimers() {
        stopGetBlocksTimer();
        stopGetTxTimer();
    }

    public void setSyncStatus(SyncSatus syncStatus){
        this.syncStatus = syncStatus;
    }

    public SyncSatus getSyncStatus(){
        return syncStatus;
    }

    public void setPeerId(String peerId){
        this.peerId = peerId;
    }

    public enum SyncSatus{
        INIT,
        HASH_RETRIEVING,
        BLOCK_RETRIEVING,
        SYNC_DONE;
    }

    public void doSync(){

        logger.info("Sync force activated");
        syncStatus = SyncSatus.INIT;
        sendStatus();
    }

    public StatusMessage getHandshakeStatusMessage(){
        return handshakeStatusMessage;
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }
}