package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.sync.SyncQueue;
import org.ethereum.sync.listener.CompositeSyncListener;
import org.ethereum.sync.SyncState;
import org.ethereum.sync.SyncStatistics;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.reverse;
import static java.util.Collections.singletonList;
import static org.ethereum.net.eth.EthVersion.V62;
import static org.ethereum.net.message.ReasonCode.USELESS_PEER;
import static org.ethereum.sync.SyncState.*;
import static org.ethereum.sync.SyncState.BLOCK_RETRIEVING;
import static org.ethereum.util.BIUtil.isLessThan;
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
    protected SyncQueue queue;

    @Autowired
    protected PendingState pendingState;

    @Autowired
    protected CompositeSyncListener compositeSyncListener;

    protected EthState ethState = EthState.INIT;

    protected SyncState syncState = IDLE;
    protected boolean syncDone = false;

    /**
     * Last block hash to be asked from the peer,
     * is set on header retrieving start
     */
    protected byte[] lastHashToAsk;

    /**
     * Number and hash of best known remote block
     */
    protected BlockIdentifier bestKnownBlock;

    protected boolean commonAncestorFound = true;

    /**
     * Header list sent in GET_BLOCK_BODIES message,
     * used to create blocks from headers and bodies
     * also, is useful when returned BLOCK_BODIES msg doesn't cover all sent hashes
     * or in case when peer is disconnected
     */
    protected final List<BlockHeaderWrapper> sentHeaders = Collections.synchronizedList(new ArrayList<BlockHeaderWrapper>());

    protected final SyncStatistics syncStats = new SyncStatistics();

    protected Queue<GetBlockHeadersMessageWrapper> headerRequests = new LinkedBlockingQueue<>();

    private BlockWrapper gapBlock;

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

    protected synchronized void sendGetBlockHeaders(long blockNumber, int maxBlocksAsk) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: queue GetBlockHeaders, blockNumber [{}], maxBlocksAsk [{}]",
                channel.getPeerIdShort(),
                blockNumber,
                maxBlocksAsk
        );

        GetBlockHeadersMessage headersRequest = new GetBlockHeadersMessage(blockNumber, maxBlocksAsk);
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

    protected synchronized boolean sendGetBlockBodies() {

        List<BlockHeaderWrapper> headers = queue.pollHeaders();
        if (headers.isEmpty()) {
            if(logger.isTraceEnabled()) logger.trace(
                    "Peer {}: no more headers in queue, idle",
                    channel.getPeerIdShort()
            );
            changeState(IDLE);
            return false;
        }

        sendGetBlockBodies(headers);

        return true;
    }

    protected synchronized void sendGetBlockBodies(List<BlockHeaderWrapper> headers) {

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
                loggerNet.info("Removing EthHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
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
                loggerNet.debug("Peer discovery mode: STATUS received, disconnecting...");
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

        compositeSyncListener.onNewBlockNumber(bestKnownBlock.getNumber());

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
        pendingState.addWireTransactions(txSet);
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

        if (ethState == EthState.STATUS_SENT)
            processInitHeaders(received);
        else if (!syncDone)
            processHeaderRetrieving(received);
        else if (request.isNewHashesHandling())
            processNewBlockHeaders(received);
        else if (!commonAncestorFound)
            processForkCoverage(received);
        else
            processGapRecovery(received);

        sendNextHeaderRequest();
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

        // validateAndMerge removes merged headers
        // others are returned back
        returnHeaders();

        if (blocks == null) {

            // headers will be returned by #onShutdown()
            dropConnection();
            return;
        }

        queue.addList(blocks, channel.getNodeId());

        if (syncDone) {
            sendGetBlockBodies();
        } else if (syncState == BLOCK_RETRIEVING) {
            changeState(IDLE);
        }
    }

    protected synchronized void processNewBlock(NewBlockMessage newBlockMessage) {

        Block newBlock = newBlockMessage.getBlock();

        logger.debug("New block received: block.index [{}]", newBlock.getNumber());

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

        updateBestBlock(newBlock);

        compositeSyncListener.onNewBlockNumber(newBlock.getNumber());

        // queueing new blocks doesn't make sense
        // while Long sync is in progress
        if (!syncDone) return;

        if (!queue.validateAndAddNewBlock(newBlock, channel.getNodeId())) {
            dropConnection();
        }
    }

    /*************************
     *    Sync Management    *
     *************************/

    @Override
    public synchronized void changeState(SyncState newState) {

        if (syncState == newState) {
            return;
        }

        returnHeaders();

        logger.trace(
                "Peer {}: changing state from {} to {}",
                channel.getPeerIdShort(),
                syncState,
                newState
        );

        if (newState == HASH_RETRIEVING) {
            syncStats.reset();
            startHeaderRetrieving();
        }
        if (newState == BLOCK_RETRIEVING) {
            syncStats.reset();
            if (!sendGetBlockBodies()) {
                newState = IDLE;
            }
        }
        syncState = newState;
    }

    @Override
    public synchronized void onShutdown() {
        returnHeaders();
    }

    @Override
    public synchronized void recoverGap(BlockWrapper block) {
        syncStats.reset();
        syncState = HASH_RETRIEVING;
        startGapRecovery(block);
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

        wrapper.send();
        sendMessage(wrapper.getMessage());
    }

    protected synchronized void processInitHeaders(List<BlockHeader> received) {
        BlockHeader first = received.get(0);
        updateBestBlock(first);
        ethState = EthState.STATUS_SUCCEEDED;
        logger.trace(
                "Peer {}: init request succeeded, best known block {}",
                channel.getPeerIdShort(), bestKnownBlock
        );
    }

    protected synchronized void processHeaderRetrieving(List<BlockHeader> received) {

        // treat empty headers response as end of header sync
        if (received.isEmpty()) {
            changeState(DONE_HASH_RETRIEVING);
        } else {
            syncStats.addHeaders(received.size());

            logger.debug("Adding " + received.size() + " headers to the queue.");

            if (!queue.validateAndAddHeaders(received, channel.getNodeId())) {

                dropConnection();
                return;
            }
        }

        if (syncState == HASH_RETRIEVING) {
            BlockHeader latest = received.get(received.size() - 1);
            sendGetBlockHeaders(latest.getNumber() + 1, maxHashesAsk);
        }

        if (syncState == DONE_HASH_RETRIEVING) {
            logger.info(
                    "Peer {}: header sync completed, [{}] headers in queue",
                    channel.getPeerIdShort(),
                    queue.headerStoreSize()
            );
        }
    }

    protected synchronized void processNewBlockHeaders(List<BlockHeader> received) {

        logger.debug("Adding " + received.size() + " headers to the queue.");

        if (!queue.validateAndAddHeaders(received, channel.getNodeId()))
            dropConnection();

        changeState(BLOCK_RETRIEVING);
    }

    protected synchronized void processGapRecovery(List<BlockHeader> received) {

        boolean completed = false;

        // treat empty headers response as end of header sync
        if (received.isEmpty()) {
            completed = true;
        } else {
            syncStats.addHeaders(received.size());

            List<BlockHeader> adding = new ArrayList<>(received.size());
            for(BlockHeader header : received) {

                adding.add(header);

                if (Arrays.equals(header.getHash(), lastHashToAsk)) {
                    completed = true;
                    logger.trace("Peer {}: got terminal hash [{}]", channel.getPeerIdShort(), toHexString(lastHashToAsk));
                    break;
                }
            }

            logger.debug("Adding " + adding.size() + " headers to the queue.");

            if (!queue.validateAndAddHeaders(adding, channel.getNodeId())) {

                dropConnection();
                return;
            }
        }

        if (completed) {
            logger.info(
                    "Peer {}: header sync completed, [{}] headers in queue",
                    channel.getPeerIdShort(),
                    queue.headerStoreSize()
            );
            changeState(BLOCK_RETRIEVING);
        } else {
            long lastNumber = received.get(received.size() - 1).getNumber();
            sendGetBlockHeaders(lastNumber + 1, maxHashesAsk);
        }
    }

    protected synchronized void startHeaderRetrieving() {

        lastHashToAsk = null;
        commonAncestorFound = true;

        if (logger.isInfoEnabled()) logger.info(
                "Peer {}: HASH_RETRIEVING initiated, askLimit [{}]",
                channel.getPeerIdShort(),
                maxHashesAsk
        );

        BlockWrapper latest = queue.peekLastBlock();

        long blockNumber = latest != null ? latest.getNumber() : bestBlock.getNumber();

        sendGetBlockHeaders(blockNumber + 1, maxHashesAsk);
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

    private void updateBestBlock(Block block) {
        bestKnownBlock = new BlockIdentifier(block.getHash(), block.getNumber());
    }

    private void updateBestBlock(BlockHeader header) {
        bestKnownBlock = new BlockIdentifier(header.getHash(), header.getNumber());
    }

    private void updateBestBlock(List<BlockIdentifier> identifiers) {

        for (BlockIdentifier id : identifiers)
            if (bestKnownBlock == null || id.getNumber() > bestKnownBlock.getNumber()) {
                bestKnownBlock = id;
            }
    }

    /*************************
     *     Fork Coverage     *
     *************************/

    private static final int FORK_COVER_BATCH_SIZE = 192;

    protected synchronized void startGapRecovery(BlockWrapper block) {

        gapBlock = block;
        lastHashToAsk = gapBlock.getHash();

        if (logger.isInfoEnabled()) logger.info(
                "Peer {}: HASH_RETRIEVING initiated, lastHashToAsk [{}], askLimit [{}]",
                channel.getPeerIdShort(),
                toHexString(lastHashToAsk),
                maxHashesAsk
        );

        commonAncestorFound = false;

        if (isNegativeGap()) {

            logger.trace("Peer {}: start fetching remote fork", channel.getPeerIdShort());
            sendGetBlockHeaders(gapBlock.getHash(), FORK_COVER_BATCH_SIZE, 0, true);
            return;
        }

        logger.trace("Peer {}: start looking for common ancestor", channel.getPeerIdShort());

        long bestNumber = bestBlock.getNumber();
        long blockNumber = max(0, bestNumber - FORK_COVER_BATCH_SIZE + 1);
        sendGetBlockHeaders(blockNumber, min(FORK_COVER_BATCH_SIZE, (int) (bestNumber - blockNumber + 1)));
    }

    private void processForkCoverage(List<BlockHeader> received) {

        if (!isNegativeGap()) reverse(received);

        ListIterator<BlockHeader> it = received.listIterator();

        if (isNegativeGap()) {

            // gap block didn't come, drop remote peer
            if (!Arrays.equals(it.next().getHash(), gapBlock.getHash())) {

                logger.info("Peer {}: invalid response, gap block is missed", channel.getPeerIdShort());
                dropConnection();
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
                        toHexString(header.getHash())
                );

                break;
            }
            headers.add(header);
        }

        if (!commonAncestorFound) {

            logger.info("Peer {}: invalid response, common ancestor is not found", channel.getPeerIdShort());
            dropConnection();
            return;
        }

        // add missed headers
        queue.validateAndAddHeaders(headers, channel.getNodeId());

        if (isNegativeGap()) {

            // fork headers should already be fetched here
            logger.trace("Peer {}: remote fork is fetched", channel.getPeerIdShort());
            changeState(BLOCK_RETRIEVING);
            return;
        }

        // start header sync
        sendGetBlockHeaders(bestBlock.getNumber() + 1, maxHashesAsk);
    }

    private boolean isNegativeGap() {

        if (gapBlock == null) return false;

        return gapBlock.getNumber() <= bestBlock.getNumber();
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
        return ethState.ordinal() > EthState.STATUS_SENT.ordinal();
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

        List<byte[]> bodyList = response.getBlockBodies();

        Iterator<byte[]> bodies = bodyList.iterator();
        Iterator<BlockHeaderWrapper> wrappers = sentHeaders.iterator();

        List<Block> blocks = new ArrayList<>(bodyList.size());
        List<BlockHeaderWrapper> coveredHeaders = new ArrayList<>(sentHeaders.size());

        while (bodies.hasNext() && wrappers.hasNext()) {
            BlockHeaderWrapper wrapper = wrappers.next();
            byte[] body = bodies.next();

            Block b = new Block.Builder()
                    .withHeader(wrapper.getHeader())
                    .withBody(body)
                    .create();

            // handle invalid merge
            if (b == null) {

                if (logger.isInfoEnabled()) logger.info(
                        "Peer {}: invalid response to [GET_BLOCK_BODIES], header {} can't be merged with body {}",
                        channel.getPeerIdShort(), wrapper.getHeader(), toHexString(body)
                );
                return null;
            }

            coveredHeaders.add(wrapper);
            blocks.add(b);
        }

        // remove headers covered by response
        sentHeaders.removeAll(coveredHeaders);

        return blocks;
    }

    private boolean isValid(BlockBodiesMessage response) {

        // against best known block,
        // if short sync is in progress there might be a case when
        // peer have no bodies even for blocks with lower number than best known
        if (!syncDone) {

            int expectedCount = 0;
            if (sentHeaders.size() > 0 &&
                    sentHeaders.get(sentHeaders.size() - 1).getNumber() <= bestKnownBlock.getNumber()) {
                expectedCount = sentHeaders.size();
            } else if (sentHeaders.size() > 0 && sentHeaders.get(0).getNumber() > bestKnownBlock.getNumber()) {
                expectedCount = 0;
            } else {
                for (int i = 0; i < sentHeaders.size(); i++)
                    if (sentHeaders.get(i).getNumber() <= bestKnownBlock.getNumber()) {
                        expectedCount = i;
                    } else {
                        break;
                    }
            }

            if (response.getBlockBodies().size() < expectedCount) {
                if (logger.isInfoEnabled()) logger.info(
                        "Peer {}: invalid response to [GET_BLOCK_BODIES], expected count {}, got {}",
                        channel.getPeerIdShort(), expectedCount, response.getBlockBodies().size()
                );
                return false;
            }
        }

        // check if peer didn't return a body
        // corresponding to the header sent previously
        if (response.getBlockBodies().size() < sentHeaders.size()) {
            BlockHeaderWrapper header = sentHeaders.get(response.getBlockBodies().size());
            if (header.sentBy(channel.getNodeId())) {

                if (logger.isInfoEnabled()) logger.info(
                        "Peer {}: invalid response to [GET_BLOCK_BODIES], body for {} wasn't returned",
                        channel.getPeerIdShort(), toHexString(header.getHash())
                );
                return false;
            }
        }

        return true;
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
            if (ethState == EthState.STATUS_SENT) {
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

        // drop headers and blocks during Short sync only
        if (syncDone) {
            queue.dropHeaders(channel.getNodeId());
            queue.dropBlocks(channel.getNodeId());
        }

        logger.info("Peer {}: is a bad one, drop", channel.getPeerIdShort());
        disconnect(USELESS_PEER);
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
                    "Peer {}: [ {}, {}, blocks {}, ping {} ms ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState,
                    syncStats.getBlocksCount(),
                    String.format("%.2f", channel.getPeerStats().getAvgLatency())); break;
            case HASH_RETRIEVING: logger.info(
                    "Peer {}: [ {}, {}, headers {}, ping {} ms ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState,
                    syncStats.getHeadersCount(),
                    String.format("%.2f", channel.getPeerStats().getAvgLatency())); break;
            default: logger.info(
                    "Peer {}: [ {}, state {}, ping {} ms ]",
                    version,
                    channel.getPeerIdShort(),
                    syncState,
                    String.format("%.2f", channel.getPeerStats().getAvgLatency()));
        }
    }

    protected enum EthState {
        INIT,
        STATUS_SENT,
        STATUS_SUCCEEDED,
        STATUS_FAILED
    }
}
