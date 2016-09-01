package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.sync.SyncManager;
import org.ethereum.sync.SyncState;
import org.ethereum.sync.SyncStatistics;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Math.exp;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.singletonList;
import static org.ethereum.net.eth.EthVersion.V62;
import static org.ethereum.net.message.ReasonCode.USELESS_PEER;
import static org.ethereum.sync.SyncState.*;
import static org.ethereum.sync.SyncState.BLOCK_RETRIEVING;
import static org.spongycastle.util.encoders.Hex.toHexString;

/**
 * Eth 62
 *
 * @author Mikhail Kalinin
 * @since 04.09.2015
 */
@Component
@Scope("prototype")
public class Eth62 extends EthHandler {

    protected static final int MAX_HASHES_TO_SEND = 65536;

    private final static Logger logger = LoggerFactory.getLogger("sync");
    private final static Logger loggerNet = LoggerFactory.getLogger("net");

    @Autowired
    protected BlockStore blockstore;

    @Autowired
    protected SyncManager syncManager;

    @Autowired
    protected PendingState pendingState;

    @Autowired
    protected NodeManager nodeManager;

    protected EthState ethState = EthState.INIT;

    protected SyncState syncState = IDLE;
    protected boolean syncDone = false;

    /**
     * Number and hash of best known remote block
     */
    protected BlockIdentifier bestKnownBlock;
    private BigInteger totalDifficulty;

    /**
     * Header list sent in GET_BLOCK_BODIES message,
     * used to create blocks from headers and bodies
     * also, is useful when returned BLOCK_BODIES msg doesn't cover all sent hashes
     * or in case when peer is disconnected
     */
    protected final List<BlockHeaderWrapper> sentHeaders = Collections.synchronizedList(new ArrayList<BlockHeaderWrapper>());

    protected final SyncStatistics syncStats = new SyncStatistics();

    protected Queue<GetBlockHeadersMessageWrapper> headerRequests = new LinkedBlockingQueue<>();

    private Map<Long, byte[]> blockHashCheck;

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
    public synchronized void sendStatus() {
        byte protocolVersion = version.getCode();
        int networkId = config.networkId();

        BigInteger totalDifficulty = blockchain.getTotalDifficulty();
        byte[] bestHash = blockchain.getBestBlockHash();
        StatusMessage msg = new StatusMessage(protocolVersion, networkId,
                ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, config.getGenesis().getHash());
        sendMessage(msg);

        ethState = EthState.STATUS_SENT;

        sendNextHeaderRequest();
    }

    @Override
    public synchronized void sendNewBlockHashes(Block block) {

        BlockIdentifier identifier = new BlockIdentifier(block.getHash(), block.getNumber());
        NewBlockHashesMessage msg = new NewBlockHashesMessage(singletonList(identifier));
        sendMessage(msg);
    }

    @Override
    public synchronized void sendTransaction(List<Transaction> txs) {
        TransactionsMessage msg = new TransactionsMessage(txs);
        sendMessage(msg);
    }

    @Override
    public synchronized void sendGetBlockHeaders(long blockNumber, int maxBlocksAsk, boolean reverse) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: queue GetBlockHeaders, blockNumber [{}], maxBlocksAsk [{}]",
                channel.getPeerIdShort(),
                blockNumber,
                maxBlocksAsk
        );

        GetBlockHeadersMessage headersRequest = new GetBlockHeadersMessage(blockNumber, null, maxBlocksAsk, 0, reverse);
        headerRequests.add(new GetBlockHeadersMessageWrapper(headersRequest));

        sendNextHeaderRequest();
    }

    protected synchronized void sendGetBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse) {
        sendGetBlockHeaders(blockHash, maxBlocksAsk, skip, reverse, false);
    }

    protected synchronized void sendGetNewBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse) {
        sendGetBlockHeaders(blockHash, maxBlocksAsk, skip, reverse, true);
    }

    protected synchronized void sendGetBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse, boolean newHashes) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: queue GetBlockHeaders, blockHash [{}], maxBlocksAsk [{}], skip[{}], reverse [{}]",
                channel.getPeerIdShort(),
                "0x" + toHexString(blockHash).substring(0, 8),
                maxBlocksAsk, skip, reverse
        );

        GetBlockHeadersMessage headersRequest = new GetBlockHeadersMessage(0, blockHash, maxBlocksAsk, skip, reverse);
        headerRequests.add(new GetBlockHeadersMessageWrapper(headersRequest, newHashes));

        sendNextHeaderRequest();
    }

    @Override
    public synchronized void sendGetBlockBodies(List<BlockHeaderWrapper> headers) {
        syncState = BLOCK_RETRIEVING;
        sentHeaders.clear();
        sentHeaders.addAll(headers);

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send GetBlockBodies, hashes.count [{}]",
                channel.getPeerIdShort(),
                sentHeaders.size()
        );

        List<byte[]> hashes = new ArrayList<>(headers.size());
        for (BlockHeaderWrapper header : headers) {
            hashes.add(header.getHash());
        }

        GetBlockBodiesMessage msg = new GetBlockBodiesMessage(hashes);

        sendMessage(msg);
    }

    @Override
    public synchronized void sendNewBlock(Block block) {
        BigInteger parentTD = blockstore.getTotalDifficultyForHash(block.getParentHash());
        byte[] td = ByteUtil.bigIntegerToBytes(parentTD.add(new BigInteger(1, block.getDifficulty())));
        NewBlockMessage msg = new NewBlockMessage(block, td);
        sendMessage(msg);
    }

    /*************************
     *  Message Processing   *
     *************************/

    protected synchronized void processStatus(StatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {

        try {

            if (!Arrays.equals(msg.getGenesisHash(), config.getGenesis().getHash())
                    || msg.getProtocolVersion() != version.getCode()) {
                if (!peerDiscoveryMode) {
                    loggerNet.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
                }
                ethState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);
                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
                return;
            }

            if (msg.getNetworkId() != config.networkId()) {
                ethState = EthState.STATUS_FAILED;
                disconnect(ReasonCode.NULL_IDENTITY);
                return;
            }

            // basic checks passed, update statistics
            channel.getNodeStatistics().ethHandshake(msg);
            ethereumListener.onEthStatusUpdated(channel, msg);

            if (peerDiscoveryMode) {
                loggerNet.trace("Peer discovery mode: STATUS received, disconnecting...");
                disconnect(ReasonCode.REQUESTED);
                ctx.close().sync();
                ctx.disconnect().sync();
                return;
            }

            // update bestKnownBlock info
            sendGetBlockHeaders(msg.getBestHash(), 1, 0, false);

        } catch (NoSuchElementException e) {
            loggerNet.debug("EthHandler already removed");
        }
    }

    protected synchronized void processNewBlockHashes(NewBlockHashesMessage msg) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing NewBlockHashes, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockIdentifiers().size()
        );

        List<BlockIdentifier> identifiers = msg.getBlockIdentifiers();

        if (identifiers.isEmpty()) return;

        updateBestBlock(identifiers);

        // queueing new blocks doesn't make sense
        // while Long sync is in progress
        if (!syncDone) return;

        if (syncState != HASH_RETRIEVING) {
            BlockIdentifier first = identifiers.get(0);
            long lastBlockNumber = identifiers.get(identifiers.size() - 1).getNumber();
            int maxBlocksAsk = (int) (lastBlockNumber - first.getNumber() + 1);
            sendGetNewBlockHeaders(first.getHash(), maxBlocksAsk, 0, false);
        }
    }

    protected synchronized void processTransactions(TransactionsMessage msg) {
        if(!processTransactions) {
            return;
        }

        List<Transaction> txSet = msg.getTransactions();
        pendingState.addPendingTransactions(txSet);
    }

    protected synchronized void processGetBlockHeaders(GetBlockHeadersMessage msg) {
        List<BlockHeader> headers = blockchain.getListOfHeadersStartFrom(
                msg.getBlockIdentifier(),
                msg.getSkipBlocks(),
                min(msg.getMaxHeaders(), MAX_HASHES_TO_SEND),
                msg.isReverse()
        );

        BlockHeadersMessage response = new BlockHeadersMessage(headers);
        sendMessage(response);
    }

    protected synchronized void processBlockHeaders(BlockHeadersMessage msg) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing BlockHeaders, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockHeaders().size()
        );

        GetBlockHeadersMessageWrapper request = headerRequests.poll();

        if (!isValid(msg, request)) {

            dropConnection();
            return;
        }

        List<BlockHeader> received = msg.getBlockHeaders();

        if (ethState == EthState.STATUS_SENT || ethState == EthState.HASH_CONSTRAINTS_CHECK)
            processInitHeaders(received);
        else {
            syncStats.addHeaders(received.size());

            logger.debug("Adding " + received.size() + " headers to the queue.");

            if (!syncManager.validateAndAddHeaders(received, channel.getNodeId())) {

                dropConnection();
                return;
            }
        }

        syncState = IDLE;
    }

    protected synchronized void processGetBlockBodies(GetBlockBodiesMessage msg) {
        List<byte[]> bodies = blockchain.getListOfBodiesByHashes(msg.getBlockHashes());

        BlockBodiesMessage response = new BlockBodiesMessage(bodies);
        sendMessage(response);
    }

    protected synchronized void processBlockBodies(BlockBodiesMessage msg) {

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: process BlockBodies, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockBodies().size()
        );

        if (!isValid(msg)) {

            dropConnection();
            return;
        }

        syncStats.addBlocks(msg.getBlockBodies().size());

        List<Block> blocks = validateAndMerge(msg);

        if (blocks == null) {

            // headers will be returned by #onShutdown()
            dropConnection();
            return;
        }

        syncManager.addList(blocks, channel.getNodeId());

        syncState = IDLE;
    }

    protected synchronized void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        logger.debug("New block received: block.index [{}]", newBlock.getNumber());

        updateTotalDifficulty(newBlockMessage.getDifficultyAsBigInt());

        updateBestBlock(newBlock);

        if (!syncManager.validateAndAddNewBlock(newBlock, channel.getNodeId())) {
            dropConnection();
        }
    }

    /*************************
     *    Sync Management    *
     *************************/

    @Override
    public synchronized void onShutdown() {
    }

    @Override
    public synchronized void fetchBodies(List<BlockHeaderWrapper> headers) {
        syncStats.reset();
        syncState = BLOCK_RETRIEVING;
        sendGetBlockBodies(headers);
    }

    protected synchronized void sendNextHeaderRequest() {

        // do not send header requests if status hasn't been passed yet
        if (ethState == EthState.INIT) return;

        GetBlockHeadersMessageWrapper wrapper = headerRequests.peek();

        if (wrapper == null || wrapper.isSent()) return;

        syncState = HASH_RETRIEVING;

        wrapper.send();
        sendMessage(wrapper.getMessage());
    }

    protected synchronized void processInitHeaders(List<BlockHeader> received) {

        BlockHeader first = received.get(0);
        if (ethState == EthState.STATUS_SENT) {
            updateBestBlock(first);

            logger.trace("Peer {}: init request succeeded, best known block {}",
                    channel.getPeerIdShort(), bestKnownBlock);

            // checking if the peer has expected block hashes
            ethState = EthState.HASH_CONSTRAINTS_CHECK;

            List<Pair<Long, byte[]>> constraints = config.getBlockchainConfig().
                    getConfigForBlock(first.getNumber()).blockHashConstraints();

            blockHashCheck = Collections.synchronizedMap(new HashMap<Long, byte[]>());
            for (Pair<Long, byte[]> constraint : constraints) {
                if (constraint.getLeft() <= getBestKnownBlock().getNumber()) {
                    blockHashCheck.put(constraint.getLeft(), constraint.getRight());
                    sendGetBlockHeaders(constraint.getLeft(), 1, false);
                }
            }

            logger.trace("Peer " + channel.getPeerIdShort() + ": Requested " + blockHashCheck.size() +
                    " headers for hash check: " + blockHashCheck.keySet());

        } else {
            byte[] expectedHash = blockHashCheck.get(first.getNumber());
            if (expectedHash != null) {
                if (FastByteComparisons.equal(expectedHash, first.getHash())) {
                    blockHashCheck.remove(first.getNumber());
                } else {
                    logger.debug("Peer " + channel.getPeerIdShort() + ": wrong fork (expected block " +
                            first.getNumber() + " hash " + Hex.toHexString(expectedHash) + ", but got " +
                            Hex.toHexString(first.getHash()) + ". Drop the peer and reduce reputation.");
                    channel.getNodeStatistics().wrongFork = true;
                    dropConnection();
                }
            }
        }

        if (blockHashCheck.isEmpty()) {
            ethState = EthState.STATUS_SUCCEEDED;

            logger.trace("Peer {}: all validations passed", channel.getPeerIdShort());
        }
    }

    private void updateBestBlock(Block block) {
        updateBestBlock(block.getHeader());
    }

    private void updateBestBlock(BlockHeader header) {
        if (bestKnownBlock == null || header.getNumber() > bestKnownBlock.getNumber()) {
            bestKnownBlock = new BlockIdentifier(header.getHash(), header.getNumber());
        }
    }

    private void updateBestBlock(List<BlockIdentifier> identifiers) {

        for (BlockIdentifier id : identifiers)
            if (bestKnownBlock == null || id.getNumber() > bestKnownBlock.getNumber()) {
                bestKnownBlock = id;
            }
    }

    @Override
    public BlockIdentifier getBestKnownBlock() {
        return bestKnownBlock;
    }

    private void updateTotalDifficulty(BigInteger totalDiff) {
        channel.getNodeStatistics().setEthTotalDifficulty(totalDiff);
        this.totalDifficulty = totalDiff;
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return totalDifficulty != null ? totalDifficulty : channel.getNodeStatistics().getEthTotalDifficulty();
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
    public boolean hasStatusPassed() {
        return ethState.ordinal() > EthState.HASH_CONSTRAINTS_CHECK.ordinal();
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
    public void onSyncDone(boolean done) {
        syncDone = done;
    }

    /*************************
     *       Validation      *
     *************************/

    @Nullable
    private List<Block> validateAndMerge(BlockBodiesMessage response) {
        // merging received block bodies with requested headers
        // the assumption is the following:
        // - response may miss any bodies present in the request
        // - response may not contain non-requested bodies
        // - order of response bodies should be preserved
        // Otherwise the response is assumed invalid and all bodies are dropped

        List<byte[]> bodyList = response.getBlockBodies();

        Iterator<byte[]> bodies = bodyList.iterator();
        Iterator<BlockHeaderWrapper> wrappers = sentHeaders.iterator();

        List<Block> blocks = new ArrayList<>(bodyList.size());
        List<BlockHeaderWrapper> coveredHeaders = new ArrayList<>(sentHeaders.size());

        boolean blockMerged = true;
        byte[] body = null;
        while (bodies.hasNext() && wrappers.hasNext()) {

            BlockHeaderWrapper wrapper = wrappers.next();
            if (blockMerged) {
                body = bodies.next();
            }

            Block b = new Block.Builder()
                    .withHeader(wrapper.getHeader())
                    .withBody(body)
                    .create();

            if (b == null) {
                blockMerged = false;
            } else {
                blockMerged = true;

                coveredHeaders.add(wrapper);
                blocks.add(b);
            }
        }

        if (bodies.hasNext()) {
            logger.info("Peer {}: invalid BLOCK_BODIES response: at least one block body doesn't correspond to any of requested headers: ",
                    channel.getPeerIdShort(), Hex.toHexString(bodies.next()));
            return null;
        }

        // remove headers covered by response
        sentHeaders.removeAll(coveredHeaders);

        return blocks;
    }

    private boolean isValid(BlockBodiesMessage response) {
        return response.getBlockBodies().size() <= sentHeaders.size();
    }

    private boolean isValid(BlockHeadersMessage response, GetBlockHeadersMessageWrapper requestWrapper) {

        GetBlockHeadersMessage request = requestWrapper.getMessage();
        List<BlockHeader> headers = response.getBlockHeaders();

        // max headers
        if (headers.size() > request.getMaxHeaders()) {

            if (logger.isInfoEnabled()) logger.info(
                    "Peer {}: invalid response to {}, exceeds maxHeaders limit, headers count={}",
                    channel.getPeerIdShort(), request, headers.size()
            );
            return false;
        }

        // emptiness against best known block
        if (headers.isEmpty()) {

            // initial call after handshake
            if (ethState == EthState.STATUS_SENT || ethState == EthState.HASH_CONSTRAINTS_CHECK) {
                if (logger.isInfoEnabled()) logger.info(
                        "Peer {}: invalid response to initial {}, empty",
                        channel.getPeerIdShort(), request
                );
                return false;
            }

            if (request.getBlockHash() == null &&
                    request.getBlockNumber() <= bestKnownBlock.getNumber()) {

                if (logger.isInfoEnabled()) logger.info(
                        "Peer {}: invalid response to {}, it's empty while bestKnownBlock is {}",
                        channel.getPeerIdShort(), request, bestKnownBlock
                );
                return false;
            }

            return true;
        }

        // first header
        BlockHeader first = headers.get(0);

        if (request.getBlockHash() != null) {

            if (request.getSkipBlocks() == 0) {
                if (!Arrays.equals(request.getBlockHash(), first.getHash())) {

                    if (logger.isInfoEnabled()) logger.info(
                            "Peer {}: invalid response to {}, first header is invalid {}",
                            channel.getPeerIdShort(), request, first
                    );
                    return false;
                }
            }

        } else {

            long expectedNum = request.getBlockNumber() + request.getSkipBlocks();
            if (expectedNum != first.getNumber()) {

                if (logger.isInfoEnabled()) logger.info(
                        "Peer {}: invalid response to {}, first header is invalid {}",
                        channel.getPeerIdShort(), request, first
                );
                return false;
            }
        }

        // skip following checks in case of NEW_BLOCK_HASHES handling
        if (requestWrapper.isNewHashesHandling()) return true;

        // numbers and ancestors
        if (request.isReverse()) {

            for (int i = 1; i < headers.size(); i++) {

                BlockHeader cur = headers.get(i);
                BlockHeader prev = headers.get(i - 1);

                long num = cur.getNumber();
                long expectedNum = prev.getNumber() - 1;

                if (num != expectedNum) {
                    if (logger.isInfoEnabled()) logger.info(
                            "Peer {}: invalid response to {}, got #{}, expected #{}",
                            channel.getPeerIdShort(), request, num, expectedNum
                    );
                    return false;
                }

                if (!Arrays.equals(prev.getParentHash(), cur.getHash())) {
                    if (logger.isInfoEnabled()) logger.info(
                            "Peer {}: invalid response to {}, got parent hash {} for #{}, expected {}",
                            channel.getPeerIdShort(), request, toHexString(prev.getParentHash()),
                            prev.getNumber(), toHexString(cur.getHash())
                            );
                    return false;
                }
            }
        } else {

            for (int i = 1; i < headers.size(); i++) {

                BlockHeader cur = headers.get(i);
                BlockHeader prev = headers.get(i - 1);

                long num = cur.getNumber();
                long expectedNum = prev.getNumber() + 1;

                if (num != expectedNum) {
                    if (logger.isInfoEnabled()) logger.info(
                            "Peer {}: invalid response to {}, got #{}, expected #{}",
                            channel.getPeerIdShort(), request, num, expectedNum
                    );
                    return false;
                }

                if (!Arrays.equals(cur.getParentHash(), prev.getHash())) {
                    if (logger.isInfoEnabled()) logger.info(
                            "Peer {}: invalid response to {}, got parent hash {} for #{}, expected {}",
                            channel.getPeerIdShort(), request, toHexString(cur.getParentHash()),
                            cur.getNumber(), toHexString(prev.getHash())
                    );
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public synchronized void dropConnection() {

        // todo: reduce reputation

        logger.info("Peer {}: is a bad one, drop", channel.getPeerIdShort());
        disconnect(USELESS_PEER);
    }

    /*************************
     *       Logging         *
     *************************/

    @Override
    public String getSyncStats() {

        return String.format(
                "Peer %s: [ %s, %16s, ping %6s ms, difficulty %s, best block %s ]",
                version,
                channel.getPeerIdShort(),
                syncState,
                (int)channel.getPeerStats().getAvgLatency(),
                getTotalDifficulty(),
                getBestKnownBlock().getNumber());
    }

    protected enum EthState {
        INIT,
        STATUS_SENT,
        HASH_CONSTRAINTS_CHECK,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}
