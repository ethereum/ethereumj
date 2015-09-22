package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockIdentifier;
import org.ethereum.net.eth.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.Math.max;
import static org.ethereum.net.eth.EthVersion.V62;
import static org.ethereum.sync.SyncStateName.*;
import static org.ethereum.sync.SyncStateName.BLOCK_RETRIEVING;
import static org.ethereum.util.BIUtil.isMoreThan;

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

    private static final int FORK_COVER_BATCH_SIZE = 512;

    /**
     * Header list sent in GET_BLOC_BODIES message,
     * useful if returned BLOCKS msg doesn't cover all sent hashes
     * or in case when peer is disconnected
     */
    private final List<BlockHeader> sentHeaders = Collections.synchronizedList(new ArrayList<BlockHeader>());

    private boolean commonAncestorFound = false;

    public Eth62() {
        super(V62);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        switch (msg.getCommand()) {
            case NEW_BLOCK_HASHES:
                processNewBlockHashes((NewBlockHashes62Message) msg);
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
            default:
                break;
        }
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        returnHeaders();
    }

    @Override
    protected void startHashRetrieving() {
        startForkCoverage();

        long bestNumber = blockchain.getBestBlock().getNumber();
        sendGetBlockHeaders(bestNumber + 1, maxHashesAsk);
    }

    @Override
    protected boolean startBlockRetrieving() {
        return sendGetBlockBodies();
    }

    private void processNewBlockHashes(NewBlockHashes62Message msg) {

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

            if (!queue.isBlockExist(identifier.getHash())
                && !blockchain.isBlockExist(identifier.getHash())) {

                long lastBlockNumber = identifiers.get(identifiers.size() - 1).getNumber();
                int maxBlocksAsk = (int) (lastBlockNumber - identifier.getNumber() + 1);
                sendGetBlockHeaders(identifier.getNumber(), maxBlocksAsk);
                return;
            }
        }
    }

    private void processGetBlockHeaders(GetBlockHeadersMessage msg) {
        List<BlockHeader> headers = blockchain.getListOfHeadersStartFrom(
                msg.getBlockIdentifier(),
                msg.getSkipBlocks(),
                msg.getMaxHeaders(),
                msg.isReverse()
        );

        BlockHeadersMessage response = new BlockHeadersMessage(headers);
        sendMessage(response);
    }

    private void processBlockHeaders(BlockHeadersMessage msg) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing BlockHeaders, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockHeaders().size()
        );

        List<BlockHeader> received = msg.getBlockHeaders();

        // treat empty headers response as end of hash sync
        // only if main sync done
        if (received.isEmpty() && (syncDone || blockchain.isBlockExist(bestHash))) {
            changeState(DONE_HASH_RETRIEVING);
        } else {
            syncStats.addHashes(received.size());

            if (received.isEmpty()) {
                return;
            }

            if (!commonAncestorFound) {
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

    private void processGetBlockBodies(GetBlockBodiesMessage msg) {
        List<byte[]> bodies = blockchain.getListOfBodiesByHashes(msg.getBlockHashes());

        BlockBodiesMessage response = new BlockBodiesMessage(bodies);
        sendMessage(response);
    }

    private void processBlockBodies(BlockBodiesMessage msg) {

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

                // update TD and best hash
                if (isMoreThan(block.getDifficultyBI(), channel.getTotalDifficulty())) {
                    bestHash = block.getHash();
                    channel.getNodeStatistics().setEthTotalDifficulty(block.getDifficultyBI());
                }

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

    private void sendGetBlockHeaders(long blockNumber, int maxBlocksAsk) {

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send GetBlockHeaders, blockNumber [{}], maxBlocksAsk [{}]",
                channel.getPeerIdShort(),
                blockNumber,
                maxHashesAsk
        );

        GetBlockHeadersMessage msg = new GetBlockHeadersMessage(blockNumber, maxBlocksAsk);

        sendMessage(msg);
    }

    private boolean sendGetBlockBodies() {

        List<BlockHeader> headers = queue.pollHeaders();
        if (headers.isEmpty()) {
            if(logger.isInfoEnabled()) logger.trace(
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

    /************************
     *     Fork Coverage     *
     *************************/


    private void startForkCoverage() {

        commonAncestorFound = false;

        logger.trace("Peer {}: start looking for common ancestor", channel.getPeerIdShort());

        long bestNumber = blockchain.getBestBlock().getNumber();
        long blockNumber = max(0, bestNumber - FORK_COVER_BATCH_SIZE);
        sendGetBlockHeaders(blockNumber, FORK_COVER_BATCH_SIZE);
    }

    private void maintainForkCoverage(List<BlockHeader> received) {

        long blockNumber = max(0, received.get(0).getNumber() - FORK_COVER_BATCH_SIZE);

        // start downloading hashes from blockNumber of the block with known hash
        ListIterator<BlockHeader> it = received.listIterator(received.size());
        while (it.hasPrevious()) {
            BlockHeader header = it.previous();
            if (blockchain.isBlockExist(header.getHash())) {
                commonAncestorFound = true;
                blockNumber = header.getNumber() + 1;

                logger.trace(
                        "Peer {}: common ancestor found: block.number {}, block.hash {}",
                        channel.getPeerIdShort(),
                        header.getNumber(),
                        Hex.toHexString(header.getHash())
                );

                break;
            }
        }


        if (commonAncestorFound) {

            // start header sync
            sendGetBlockHeaders(blockNumber, maxHashesAsk);

        } else {

            // continue fork coverage
            logger.trace("Peer {}: common ancestor is not found yet", channel.getPeerIdShort());
            sendGetBlockHeaders(blockNumber, FORK_COVER_BATCH_SIZE);

        }
    }
}
