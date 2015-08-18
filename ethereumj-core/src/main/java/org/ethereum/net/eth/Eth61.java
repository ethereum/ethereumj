package org.ethereum.net.eth;

import org.ethereum.net.eth.message.BlockHashesMessage;
import org.ethereum.net.eth.message.GetBlockHashesByNumberMessage;
import org.ethereum.net.eth.message.NewBlockHashesMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ethereum.net.eth.EthVersion.*;

/**
 * Implements Version 61 of Eth protocol
 *
 * @author Mikhail Kalinin
 * @since 18.08.2015
 */
public class Eth61 extends Eth {

    private static final Logger logger = LoggerFactory.getLogger("sync");

    public Eth61() {
        super(V61);
    }

    @Override
    void processNewBlockHashes(NewBlockHashesMessage newBlockHashesMessage) {
        if(logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing NEW block hashes, size [{}]",
                handler.getPeerIdShort(),
                newBlockHashesMessage.getBlockHashes().size()
        );

        List<byte[]> hashes = newBlockHashesMessage.getBlockHashes();
        if (hashes.isEmpty()) {
            return;
        }

        this.bestHash = hashes.get(hashes.size() - 1);

        queue.addNewBlockHashes(hashes);
        queue.logHashQueueSize();
    }

    @Override
    void processGetBlockHashesByNumber(GetBlockHashesByNumberMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFromBlock(msg.getBlockNumber(), msg.getMaxBlocks());

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        handler.sendMessage(msgHashes);
    }
}
