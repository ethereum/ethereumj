package org.ethereum.net.eth.handler;

import org.ethereum.core.Block;
import org.ethereum.net.eth.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.*;
import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.eth.EthVersion.*;
import static org.ethereum.net.eth.sync.SyncStateName.DONE_HASH_RETRIEVING;

/**
 * Eth V61
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
@Component
@Scope("prototype")
public class Eth61 extends EthHandler {

    private static final Logger logger = LoggerFactory.getLogger("sync");

    private static final int FORK_COVER_BATCH_SIZE = 512;

    /**
     * Last blockNumber value sent within GET_BLOCK_HASHES_BY_NUMBER msg
     */
    private long lastAskedNumber = 0;

    /**
     * In Eth 61 we have an ability to check if we're on the fork
     * before starting hash sync.
     *
     * To do this we just download hashes of already known blocks
     * from remote peer with best chain and comparing those hashes against ours.
     *
     * If best peer's hashes differ from ours then we're on the fork
     * and trying to jump back to canonical chain
     */
    // TODO: we need to handle bad peers somehow, cause it may revert us to the very beginning
    private boolean forkCovered = false;

    public Eth61() {
        super(V61);
    }

    @Override
    protected void processBlockHashes(List<byte[]> received) {
        if (received.isEmpty()) {
            return;
        }

        if (!forkCovered) {
            maintainForkCoverage(received);
            return;
        }

        queue.addHashesLast(received);

        for(byte[] hash : received)
            if (Arrays.equals(hash, lastHashToAsk)) {
                changeState(DONE_HASH_RETRIEVING);
                logger.trace("Peer {}: got terminal hash [{}]", channel.getPeerIdShort(), Hex.toHexString(lastHashToAsk));
                return;
            }

        long blockNumber = lastAskedNumber + min(received.size(), maxHashesAsk);
        sendGetBlockHashesByNumber(blockNumber, maxHashesAsk);
    }

    private void sendGetBlockHashesByNumber(long blockNumber, int maxHashesAsk) {
        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: send get block hashes by number, blockNumber [{}], maxHashesAsk [{}]",
                channel.getPeerIdShort(),
                blockNumber,
                maxHashesAsk
        );

        GetBlockHashesByNumberMessage msg = new GetBlockHashesByNumberMessage(blockNumber, maxHashesAsk);
        sendMessage(msg);

        lastAskedNumber = blockNumber;
    }

    @Override
    protected void processGetBlockHashesByNumber(GetBlockHashesByNumberMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFromBlock(
                msg.getBlockNumber(),
                min(msg.getMaxBlocks(), CONFIG.maxHashesAsk())
        );

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        sendMessage(msgHashes);
    }

    @Override
    protected void startHashRetrieving() {

        forkCovered = true;
        long bestNumber = blockchain.getBestBlock().getNumber();

        if (bestNumber > 0) {

            // always assume we're on the fork if best block is not a Genesis one
            startForkCoverage();

        } else {

            // if we're at the beginning there can't be any fork
            sendGetBlockHashesByNumber(bestNumber + 1, maxHashesAsk);

        }

    }

    /************************
    *     Fork Coverage     *
    *************************/


    private void startForkCoverage() {

        forkCovered = false;

        logger.trace("Peer {}: check if our chain is on the fork", channel.getPeerIdShort());

        long bestNumber = blockchain.getBestBlock().getNumber();
        long blockNumber = max(1, bestNumber - FORK_COVER_BATCH_SIZE);
        sendGetBlockHashesByNumber(blockNumber, FORK_COVER_BATCH_SIZE);

    }

    private void maintainForkCoverage(List<byte[]> received) {

        long blockNumber = max(1, lastAskedNumber - FORK_COVER_BATCH_SIZE);

        if (lastAskedNumber > 1) {

            // start downloading hashes from blockNumber of the block with known hash
            ListIterator<byte[]> it = received.listIterator(received.size());
            while (it.hasPrevious()) {
                byte[] hash = it.previous();
                if (blockchain.isBlockExist(hash)) {
                    forkCovered = true;
                    Block block = blockchain.getBlockByHash(hash);
                    blockNumber = block.getNumber() + 1;
                }
            }

        } else {
            forkCovered = true;
        }

        if (forkCovered) {

            // start hash sync
            sendGetBlockHashesByNumber(blockNumber, maxHashesAsk);

        } else {

            // continue fork coverage
            logger.trace("Peer {}: fork is not covered yet");
            sendGetBlockHashesByNumber(blockNumber, FORK_COVER_BATCH_SIZE);

        }
    }
}
