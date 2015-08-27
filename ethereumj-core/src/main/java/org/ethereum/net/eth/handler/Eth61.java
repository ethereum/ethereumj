package org.ethereum.net.eth.handler;

import org.ethereum.net.eth.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

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

    /**
     * Last blockNumber value sent within GET_BLOCK_HASHES_BY_NUMBER msg
     */
    private long lastAskedNumber = 0;

    public Eth61() {
        super(V61);
    }

    @Override
    protected void processBlockHashes(List<byte[]> received) {
        if (received.isEmpty()) {
            return;
        }

        boolean foundTerminal = false;
        for(byte[] hash : received) {
            if (Arrays.equals(hash, lastHashToAsk)) {
                foundTerminal = true;
            }
        }

        queue.addHashesLast(received);

        if (foundTerminal) {
            changeState(DONE_HASH_RETRIEVING);
            logger.trace(
                    "Peer {}: got terminal hash [{}]",
                    channel.getPeerIdShort(),
                    Hex.toHexString(lastHashToAsk)
            );
        } else {
            long blockNumber = lastAskedNumber + Math.min(received.size(), maxHashesAsk);
            sendGetBlockHashesByNumber(blockNumber);
        }
    }

    public void sendGetBlockHashesByNumber(long blockNumber) {
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
                Math.min(msg.getMaxBlocks(), CONFIG.maxHashesAsk())
        );

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        sendMessage(msgHashes);
    }

    @Override
    protected void startHashRetrieving() {
        long blockNumber = blockchain.getBestBlock().getNumber() + 1;
        sendGetBlockHashesByNumber(blockNumber);
    }
}
