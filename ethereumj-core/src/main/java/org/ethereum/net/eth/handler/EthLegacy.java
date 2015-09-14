package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.sync.SyncStateName.*;
import static org.ethereum.sync.SyncStateName.BLOCK_RETRIEVING;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * Holds commons for legacy versions of Eth protocol <br>
 * Covers PV 60 and PV 61
 *
 * @author Mikhail Kalinin
 * @since 05.09.2015
 */
public abstract class EthLegacy extends EthHandler {

    private final static Logger logger = LoggerFactory.getLogger("sync");

    protected EthLegacy(EthVersion version) {
        super(version);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        switch (msg.getCommand()) {
            case NEW_BLOCK_HASHES:
                processNewBlockHashes((NewBlockHashesMessage) msg);
                break;
            case GET_BLOCK_HASHES:
                processGetBlockHashes((GetBlockHashesMessage) msg);
                break;
            case BLOCK_HASHES:
                dispatchBlockHashes((BlockHashesMessage) msg);
                break;
            case GET_BLOCKS:
                processGetBlocks((GetBlocksMessage) msg);
                break;
            case BLOCKS:
                processBlocks((BlocksMessage) msg);
                break;
            default:
                break;
        }
    }

    abstract protected void processBlockHashes(List<byte[]> received);

    @Override
    protected boolean startBlockRetrieving() {
        return sendGetBlocks();
    }

    private void processNewBlockHashes(NewBlockHashesMessage msg) {
        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing NEW block hashes, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockHashes().size()
        );

        List<byte[]> hashes = msg.getBlockHashes();
        if (hashes.isEmpty()) {
            return;
        }

        this.bestHash = hashes.get(hashes.size() - 1);

        queue.addNewBlockHashes(hashes);
    }

    private void processGetBlockHashes(GetBlockHashesMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFrom(
                msg.getBestHash(),
                Math.max(msg.getMaxBlocks(), CONFIG.maxHashesAsk())
        );

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        sendMessage(msgHashes);
    }

    protected void dispatchBlockHashes(BlockHashesMessage blockHashesMessage) {
        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing block hashes, size [{}]",
                channel.getPeerIdShort(),
                blockHashesMessage.getBlockHashes().size()
        );

        if (syncState != HASH_RETRIEVING) {
            return;
        }

        List<byte[]> receivedHashes = blockHashesMessage.getBlockHashes();
        syncStats.addHashes(receivedHashes.size());

        processBlockHashes(receivedHashes);

        if (logger.isInfoEnabled()) {
            if (syncState == DONE_HASH_RETRIEVING) {
                logger.info(
                        "Peer {}: hashes sync completed, [{}] hashes in queue",
                        channel.getPeerIdShort(),
                        queue.hashStoreSize()
                );
            } else {
                queue.logHashQueueSize();
            }
        }
    }

    private void processGetBlocks(GetBlocksMessage msg) {

        List<byte[]> hashes = msg.getBlockHashes();

        List<Block> blocks = new ArrayList<>(hashes.size());
        for (byte[] hash : hashes) {
            Block block = blockchain.getBlockByHash(hash);
            blocks.add(block);
        }

        BlocksMessage bm = new BlocksMessage(blocks);
        sendMessage(bm);
    }

    // Parallel download blocks based on hashQueue
    private boolean sendGetBlocks() {
        // retrieve list of block hashes from queue
        // save them locally in case the remote peer
        // will return less blocks than requested.
        List<byte[]> hashes = queue.pollHashes();
        if (hashes.isEmpty()) {
            if(logger.isInfoEnabled()) logger.info(
                    "Peer {}: no more hashes in queue, idle",
                    channel.getPeerIdShort()
            );
            changeState(IDLE);
            return false;
        }

        sentHashes.clear();
        for (byte[] hash : hashes)
            this.sentHashes.add(wrap(hash));

        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send get blocks, hashes.count [{}]",
                channel.getPeerIdShort(),
                sentHashes.size()
        );

        Collections.shuffle(hashes);
        GetBlocksMessage msg = new GetBlocksMessage(hashes);

        sendMessage(msg);

        return true;
    }

    private void processBlocks(BlocksMessage blocksMessage) {
        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: process blocks, size [{}]",
                channel.getPeerIdShort(),
                blocksMessage.getBlocks().size()
        );

        List<Block> blockList = blocksMessage.getBlocks();

        syncStats.addBlocks(blockList.size());

        // check if you got less blocks than you asked,
        // and keep the missing to ask again
        sentHashes.remove(wrap(Genesis.getInstance().getHash()));
        for (Block block : blockList){
            ByteArrayWrapper hash = wrap(block.getHash());
            sentHashes.remove(hash);
        }
        returnHashes();

        if(!blockList.isEmpty()) {
            queue.addBlocks(blockList, channel.getNodeId());
            queue.logHashQueueSize();
        } else {
            changeState(BLOCKS_LACK);
        }

        if (syncState == BLOCK_RETRIEVING) {
            sendGetBlocks();
        }
    }

    protected void sendGetBlockHashes() {
        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send get block hashes, hash [{}], maxHashesAsk [{}]",
                channel.getPeerIdShort(),
                Hex.toHexString(lastHashToAsk),
                maxHashesAsk
        );
        GetBlockHashesMessage msg = new GetBlockHashesMessage(lastHashToAsk, maxHashesAsk);
        sendMessage(msg);
    }

}
