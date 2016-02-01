package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.sync.SyncManager;
import org.ethereum.sync.SyncQueue;
import org.ethereum.sync.SyncStateName;
import org.ethereum.sync.SyncStatistics;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.reverse;
import static java.util.Collections.singletonList;
import static org.ethereum.net.eth.EthVersion.V62;
import static org.ethereum.sync.SyncStateName.*;
import static org.ethereum.sync.SyncStateName.BLOCK_RETRIEVING;
import static org.ethereum.util.BIUtil.isLessThan;

/**
 * Eth 62
 *
 * @author Mikhail Kalinin
 * @since 04.09.2015
 */
@Component
@Scope("prototype")
public class Eth62 extends EthHandler {

    private final static Logger logger = LoggerFactory.getLogger("sync");
    private final static Logger loggerNet = LoggerFactory.getLogger("net");

    @Autowired
    protected SystemProperties config;

    @Autowired
    protected Blockchain blockchain;

    @Autowired
    protected BlockStore blockstore;

    @Autowired
    protected SyncManager syncManager;

    @Autowired
    protected SyncQueue queue;

    @Autowired
    protected PendingState pendingState;

    protected EthState ethState = EthState.INIT;

    protected static final int MAX_HASHES_TO_SEND = 65536;

    private static final int BLOCKS_LACK_MAX_HITS = 5;
    private int blocksLackHits = 0;

    protected SyncStateName syncState = IDLE;
    protected boolean syncDone = false;
    protected boolean processTransactions = false;

    protected byte[] bestHash;

    /**
     * Last block hash to be asked from the peer,
     * its usage depends on Eth version
     *
     * @see Eth62
     */
    protected byte[] lastHashToAsk;
    protected int maxHashesAsk;

    protected final SyncStatistics syncStats = new SyncStatistics();

    /**
     * The number above which blocks are treated as NEW,
     * filled by data gained from NewBlockHashes and NewBlock messages
     */
    protected long newBlockLowerNumber = Long.MAX_VALUE;

    /**
     * Header list sent in GET_BLOCK_BODIES message,
     * useful if returned BLOCK_BODIES msg doesn't cover all sent hashes
     * or in case when peer is disconnected
     */
    private final List<BlockHeader> sentHeaders = Collections.synchronizedList(new ArrayList<BlockHeader>());

    private boolean commonAncestorFound = false;

    public Eth62() {
        super(V62);
    }

    @PostConstruct
    private void init() {
        maxHashesAsk = config.maxHashesAsk();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        switch (msg.getCommand()) {
            case STATUS:
                processStatus((StatusMessage) msg, ctx);
                break;
            case NEW_BLOCK_HASHES:
                processNewBlockHashes((NewBlockHashesMessage) msg);
                break;
            case TRANSACTIONS:
                processTransactions((TransactionsMessage) msg);
                break;
            case GET_BLOCK_HEADERS:
                processGetBlockHeaders((GetBlockHeadersMessage) msg);
                break;
            case BLOCK_HEADERS:
                processBlockHeaders((BlockHeadersMessage) msg);
                break;
            case GET_BLOCK_BODIES:
                processGetBlockBodies((GetBlockBodiesMessage) msg);
                break;
            case BLOCK_BODIES:
                processBlockBodies((BlockBodiesMessage) msg);
                break;
            case NEW_BLOCK:
                processNewBlock((NewBlockMessage) msg);
                break;
            default:
                break;
        }
    }

    /*************************
     *    Message Sending    *
     *************************/

    @Override
    public void sendStatus() {
        byte protocolVersion = version.getCode();
        int networkId = config.networkId();

        BigInteger totalDifficulty = blockchain.getTotalDifficulty();
        byte[] bestHash = blockchain.getBestBlockHash();
        StatusMessage msg = new StatusMessage(protocolVersion, networkId,
                ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, Blockchain.GENESIS_HASH);
        sendMessage(msg);
    }

    @Override
    public void sendNewBlockHashes(Block block) {

        BlockIdentifier identifier = new BlockIdentifier(block.getHash(), block.getNumber());
        NewBlockHashesMessage msg = new NewBlockHashesMessage(singletonList(identifier));
        sendMessage(msg);
    }

    @Override
    public void sendTransaction(List<Transaction> txs) {
        TransactionsMessage msg = new TransactionsMessage(txs);
        sendMessage(msg);
    }

    protected void sendGetBlockHeaders(long blockNumber, int maxBlocksAsk) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send GetBlockHeaders, blockNumber [{}], maxBlocksAsk [{}]",
                channel.getPeerIdShort(),
                blockNumber,
                maxBlocksAsk
        );

        GetBlockHeadersMessage msg = new GetBlockHeadersMessage(blockNumber, maxBlocksAsk);

        sendMessage(msg);
    }

    protected void sendGetBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send GetBlockHeaders, blockHash [{}], maxBlocksAsk [{}], skip[{}], reverse [{}]",
                channel.getPeerIdShort(),
                "0x" + Hex.toHexString(blockHash).substring(0, 8),
                maxBlocksAsk, skip, reverse
        );

        GetBlockHeadersMessage msg = new GetBlockHeadersMessage(0, blockHash, maxBlocksAsk, skip, reverse);

        sendMessage(msg);
    }

    protected boolean sendGetBlockBodies() {

        List<BlockHeader> headers = queue.pollHeaders();
        if (headers.isEmpty()) {
            if(logger.isTraceEnabled()) logger.trace(
                    "Peer {}: no more headers in queue, idle",
                    channel.getPeerIdShort()
            );
            changeState(IDLE);
            return false;
        }

        sentHeaders.clear();
        sentHeaders.addAll(headers);

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send GetBlockBodies, hashes.count [{}]",
                channel.getPeerIdShort(),
                sentHeaders.size()
        );

        List<byte[]> hashes = new ArrayList<>(headers.size());
        for (BlockHeader header : headers) {
            hashes.add(header.getHash());
        }

        GetBlockBodiesMessage msg = new GetBlockBodiesMessage(hashes);

        sendMessage(msg);

        return true;
    }

    @Override
    public void sendNewBlock(Block block) {
        BigInteger parentTD = blockstore.getTotalDifficultyForHash(block.getParentHash());
        byte[] td = ByteUtil.bigIntegerToBytes(parentTD.add(new BigInteger(1, block.getDifficulty())));
        NewBlockMessage msg = new NewBlockMessage(block, td);
        sendMessage(msg);
    }

    /*************************
     *  Message Processing   *
     *************************/

    protected void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {
        channel.getNodeStatistics().ethHandshake(msg);
        ethereumListener.onEthStatusUpdated(channel, msg);

        try {
            if (!Arrays.equals(msg.getGenesisHash(), Blockchain.GENESIS_HASH)
                    || msg.getProtocolVersion() != version.getCode()) {
                loggerNet.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
                ethState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);
                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
                return;
            } else if (msg.getNetworkId() != config.networkId()) {
                ethState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.NULL_IDENTITY);
                return;
            } else if (peerDiscoveryMode) {
                loggerNet.debug("Peer discovery mode: STATUS received, disconnecting...");
                disconnect(ReasonCode.REQUESTED);
                ctx.close().sync();
                ctx.disconnect().sync();
                return;
            }
        } catch (NoSuchElementException e) {
            loggerNet.debug("EthHandler already removed");
            return;
        }

        ethState = EthState.STATUS_SUCCEEDED;

        bestHash = msg.getBestHash();
    }

    protected void processNewBlockHashes(NewBlockHashesMessage msg) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing NewBlockHashes, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockIdentifiers().size()
        );

        List<BlockIdentifier> identifiers = msg.getBlockIdentifiers();

        if (identifiers.isEmpty()) {
            return;
        }

        this.bestHash = identifiers.get(identifiers.size() - 1).getHash();

        for (BlockIdentifier identifier : identifiers) {
            if (newBlockLowerNumber == Long.MAX_VALUE) {
                newBlockLowerNumber = identifier.getNumber();
            }
        }

        if (syncState != HASH_RETRIEVING) {
            long firstBlockNumber = identifiers.get(0).getNumber();
            long lastBlockNumber = identifiers.get(identifiers.size() - 1).getNumber();
            int maxBlocksAsk = (int) (lastBlockNumber - firstBlockNumber + 1);
            sendGetBlockHeaders(firstBlockNumber, maxBlocksAsk);
        }
    }

    protected void processTransactions(TransactionsMessage msg) {
        if(!processTransactions) {
            return;
        }

        List<Transaction> txSet = msg.getTransactions();
        pendingState.addWireTransactions(txSet);
    }

    protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {
        List<BlockHeader> headers = blockchain.getListOfHeadersStartFrom(
                msg.getBlockIdentifier(),
                msg.getSkipBlocks(),
                min(msg.getMaxHeaders(), MAX_HASHES_TO_SEND),
                msg.isReverse()
        );

        BlockHeadersMessage response = new BlockHeadersMessage(headers);
        sendMessage(response);
    }

    protected void processBlockHeaders(BlockHeadersMessage msg) {

        // todo check if remote peer responds with same headers on different GET_BLOCK_HEADERS

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing BlockHeaders, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockHeaders().size()
        );

        List<BlockHeader> received = msg.getBlockHeaders();

        // treat empty headers response as end of header sync
        if (received.isEmpty()) {
            changeState(DONE_HASH_RETRIEVING);
        } else {
            syncStats.addHashes(received.size());

            if (syncState == HASH_RETRIEVING && !commonAncestorFound) {
                maintainForkCoverage(received);
                return;
            }

            List<BlockHeader> adding = new ArrayList<>(received.size());
            for(BlockHeader header : received) {

                adding.add(header);

                if (Arrays.equals(header.getHash(), lastHashToAsk)) {
                    changeState(DONE_HASH_RETRIEVING);
                    logger.trace("Peer {}: got terminal hash [{}]", channel.getPeerIdShort(), Hex.toHexString(lastHashToAsk));
                    break;
                }
            }

            logger.debug("Adding " + adding.size() + " headers to the queue.");
            queue.addAndValidateHeaders(adding, channel.getNodeId());
        }

        if (syncState == HASH_RETRIEVING) {
            long lastNumber = received.get(received.size() - 1).getNumber();
            sendGetBlockHeaders(lastNumber + 1, maxHashesAsk);

            queue.logHeadersSize();
        }

        if (syncState == DONE_HASH_RETRIEVING) {
            logger.info(
                    "Peer {}: header sync completed, [{}] headers in queue",
                    channel.getPeerIdShort(),
                    queue.headerStoreSize()
            );
        }
    }

    protected void processGetBlockBodies(GetBlockBodiesMessage msg) {
        List<byte[]> bodies = blockchain.getListOfBodiesByHashes(msg.getBlockHashes());

        BlockBodiesMessage response = new BlockBodiesMessage(bodies);
        sendMessage(response);
    }

    protected void processBlockBodies(BlockBodiesMessage msg) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: process BlockBodies, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockBodies().size()
        );

        List<byte[]> bodyList = msg.getBlockBodies();

        syncStats.addBlocks(bodyList.size());

        // create blocks and add them to the queue
        Iterator<byte[]> bodies = bodyList.iterator();
        Iterator<BlockHeader> headers = sentHeaders.iterator();

        List<Block> blocks = new ArrayList<>(bodyList.size());
        List<BlockHeader> coveredHeaders = new ArrayList<>(sentHeaders.size());

        while (bodies.hasNext() && headers.hasNext()) {
            BlockHeader header = headers.next();
            byte[] body = bodies.next();

            Block b = new Block.Builder()
                    .withHeader(header)
                    .withBody(body)
                    .create();

            if (b == null) {
                break;
            }

            coveredHeaders.add(header);
            blocks.add(b);
        }

        // return headers not covered by response
        sentHeaders.removeAll(coveredHeaders);
        returnHeaders();

        if(!blocks.isEmpty()) {

            List<Block> regularBlocks = new ArrayList<>(blocks.size());

            for (Block block : blocks) {
                if (block.getNumber() < newBlockLowerNumber) {
                    regularBlocks.add(block);
                } else {
                    queue.addNew(block, channel.getNodeId());
                }
            }

            queue.addList(regularBlocks, channel.getNodeId());
            queue.logHeadersSize();
        } else {
            changeState(BLOCKS_LACK);
        }

        if (syncState == BLOCK_RETRIEVING) {
            sendGetBlockBodies();
        }
    }

    protected void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        logger.info("New block received: block.index [{}]", newBlock.getNumber());

        // skip new block if TD is lower than ours
        if (isLessThan(newBlockMessage.getDifficultyAsBigInt(), blockchain.getTotalDifficulty())) {
            logger.trace(
                    "New block difficulty lower than ours: [{}] vs [{}], skip",
                    newBlockMessage.getDifficultyAsBigInt(),
                    blockchain.getTotalDifficulty()
            );
            return;
        }

        channel.getNodeStatistics().setEthTotalDifficulty(newBlockMessage.getDifficultyAsBigInt());
        bestHash = newBlock.getHash();

        // adding block to the queue
        // there will be decided how to
        // connect it to the chain
        queue.addNew(newBlock, channel.getNodeId());

        if (newBlockLowerNumber == Long.MAX_VALUE) {
            newBlockLowerNumber = newBlock.getNumber();
        }
    }

    /*************************
     *    Sync Management    *
     *************************/

    @Override
    public void changeState(SyncStateName newState) {

        if (syncState == newState) {
            return;
        }

        logger.trace(
                "Peer {}: changing state from {} to {}",
                channel.getPeerIdShort(),
                syncState,
                newState
        );

        if (newState == HASH_RETRIEVING) {
            syncStats.reset();
            startHashRetrieving();
        }
        if (newState == BLOCK_RETRIEVING) {
            syncStats.reset();
            if (!sendGetBlockBodies()) {
                newState = IDLE;
            }
        }
        if (newState == BLOCKS_LACK) {
            if (syncDone || ++blocksLackHits < BLOCKS_LACK_MAX_HITS) {
                return;
            }
            blocksLackHits = 0; // reset
        }
        syncState = newState;
    }

    @Override
    public void onShutdown() {
        changeState(IDLE);
        returnHeaders();
    }

    private void returnHeaders() {
        if(logger.isDebugEnabled()) logger.debug(
                "Peer {}: return [{}] headers back to store",
                channel.getPeerIdShort(),
                sentHeaders.size()
        );

        synchronized (sentHeaders) {
            queue.returnHeaders(sentHeaders);
        }

        sentHeaders.clear();
    }

    /*************************
     *     Fork Coverage     *
     *************************/

    private static final int FORK_COVER_BATCH_SIZE = 192;


    protected void startHashRetrieving() {

        commonAncestorFound = false;

        if (isNegativeGap()) {

            logger.trace("Peer {}: start fetching remote fork", channel.getPeerIdShort());
            BlockWrapper gap = syncManager.getGapBlock();
            sendGetBlockHeaders(gap.getHash(), FORK_COVER_BATCH_SIZE, 0, true);
            return;
        }

        logger.trace("Peer {}: start looking for common ancestor", channel.getPeerIdShort());

        long bestNumber = blockchain.getBestBlock().getNumber();
        long blockNumber = max(0, bestNumber - FORK_COVER_BATCH_SIZE + 1);
        sendGetBlockHeaders(blockNumber, min(FORK_COVER_BATCH_SIZE, (int) (bestNumber - blockNumber + 1)));
    }

    private void maintainForkCoverage(List<BlockHeader> received) {

        if (!isNegativeGap()) reverse(received);

        ListIterator<BlockHeader> it = received.listIterator();

        if (isNegativeGap()) {

            BlockWrapper gap = syncManager.getGapBlock();

            // gap block didn't come, drop remote peer
            if (!Arrays.equals(it.next().getHash(), gap.getHash())) {

                logger.trace("Peer {}: gap block is missed in response, drop", channel.getPeerIdShort());
                syncManager.reportBadAction(channel.getNodeId());
                return;
            }
        }

        // start downloading hashes from blockNumber of the block with known hash
        List<BlockHeader> headers = new ArrayList<>();
        while (it.hasNext()) {
            BlockHeader header = it.next();
            if (blockchain.isBlockExist(header.getHash())) {
                commonAncestorFound = true;
                logger.trace(
                        "Peer {}: common ancestor found: block.number {}, block.hash {}",
                        channel.getPeerIdShort(),
                        header.getNumber(),
                        Hex.toHexString(header.getHash())
                );

                break;
            }
            headers.add(header);
        }

        if (!commonAncestorFound) {

            logger.trace("Peer {}: common ancestor is not found, drop", channel.getPeerIdShort());
            syncManager.reportBadAction(channel.getNodeId());
            return;
        }

        // add missed headers
        queue.addAndValidateHeaders(headers, channel.getNodeId());

        if (isNegativeGap()) {

            // fork headers should already be fetched here
            logger.trace("Peer {}: remote fork is fetched", channel.getPeerIdShort());
            changeState(DONE_HASH_RETRIEVING);
            return;
        }

        // start header sync
        sendGetBlockHeaders(blockchain.getBestBlock().getNumber() + 1, maxHashesAsk);
    }

    private boolean isNegativeGap() {

        if (syncManager.getGapBlock() == null) return false;

        return syncManager.getGapBlock().getNumber() <= blockchain.getBestBlock().getNumber();
    }


    /*************************
     *   Getters, setters    *
     *************************/

    @Override
    public boolean isHashRetrievingDone() {
        return syncState == DONE_HASH_RETRIEVING;
    }

    @Override
    public boolean isHashRetrieving() {
        return syncState == HASH_RETRIEVING;
    }

    @Override
    public boolean hasBlocksLack() {
        return syncState == BLOCKS_LACK;
    }

    @Override
    public boolean hasStatusPassed() {
        return ethState != EthState.INIT;
    }

    @Override
    public boolean hasStatusSucceeded() {
        return ethState == EthState.STATUS_SUCCEEDED;
    }

    @Override
    public boolean isIdle() {
        return syncState == IDLE;
    }

    @Override
    public byte[] getBestKnownHash() {
        return bestHash;
    }

    @Override
    public void setMaxHashesAsk(int maxHashesAsk) {
        this.maxHashesAsk = maxHashesAsk;
    }

    @Override
    public int getMaxHashesAsk() {
        return maxHashesAsk;
    }

    @Override
    public void setLastHashToAsk(byte[] lastHashToAsk) {
        this.lastHashToAsk = lastHashToAsk;
    }

    @Override
    public byte[] getLastHashToAsk() {
        return lastHashToAsk;
    }

    @Override
    public void enableTransactions() {
        processTransactions = true;
    }

    @Override
    public void disableTransactions() {
        processTransactions = false;
    }

    @Override
    public SyncStatistics getStats() {
        return syncStats;
    }

    @Override
    public EthVersion getVersion() {
        return version;
    }

    @Override
    public void onSyncDone() {
        syncDone = true;
    }

    /*************************
     *       Logging         *
     *************************/

    @Override
    public void logSyncStats() {
        if(!logger.isInfoEnabled()) {
            return;
        }
        switch (syncState) {
            case BLOCK_RETRIEVING: logger.info(
                    "Peer {}: [ {}, state {}, blocks count {} ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState,
                    syncStats.getBlocksCount()
            );
                break;
            case HASH_RETRIEVING: logger.info(
                    "Peer {}: [ {}, state {}, hashes count {} ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState,
                    syncStats.getHashesCount()
            );
                break;
            default: logger.info(
                    "Peer {}: [ {}, state {} ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState
            );
        }
    }

    protected enum EthState {
        INIT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}
