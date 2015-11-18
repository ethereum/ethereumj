package org.ethereum.net.eth.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.ethereum.net.eth.EthVersion.*;
import static org.ethereum.sync.SyncStateName.DONE_HASH_RETRIEVING;

/**
 * Eth V60
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
@Component
@Scope("prototype")
public class Eth60 extends EthLegacy {

    private static final Logger logger = LoggerFactory.getLogger("sync");

    public Eth60() {
        super(V60);
    }

    @Override
    protected void processBlockHashes(List<byte[]> received) {

        // todo check if remote peer responds with same hashes on different GET_BLOCK_HASHES

        if (received.isEmpty()) {
            return;
        }

        // updating last hash (hashes are inverted against block number)
        lastHashToAsk = received.get(received.size() - 1);

        byte[] foundHash = null;
        boolean foundExisting = false;
        List<byte[]> newHashes = null;
        for(int i = 0; i < received.size(); i++) {
            byte[] hash = received.get(i);
            if(blockchain.isBlockExist(hash)) {
                foundExisting = true;
                newHashes = org.ethereum.util.CollectionUtils.truncate(received, i);
                foundHash = hash;
                break;
            }
        }
        if(newHashes == null) {
            newHashes = received;
        }

        queue.addHashes(newHashes);

        if (foundExisting) {
            changeState(DONE_HASH_RETRIEVING);
            logger.trace(
                    "Peer {}: got existing hash [{}]",
                    getPeerIdShort(),
                    Hex.toHexString(foundHash)
            );
        } else {
            sendGetBlockHashes(); // another getBlockHashes with last received hash.
        }
    }

    @Override
    protected void startHashRetrieving() {
        // need to add last hash firstly
        // cause we won't get it from remote peer
        queue.addHash(lastHashToAsk);
        sendGetBlockHashes();
    }
}
