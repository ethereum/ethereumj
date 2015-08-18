package org.ethereum.net.eth.message;

import java.util.List;

/**
 * Wrapper around an Ethereum NewBlockHashes message on the network<br>
 * This message is similar to {@link BlockHashesMessage},
 * they only have different codes
 *
 * @see EthMessageCodes#NEW_BLOCK_HASHES
 *
 * @author Mikhail Kalinin
 * @since 18.08.2015
 */
public class NewBlockHashesMessage extends BlockHashesMessage {

    public NewBlockHashesMessage(List<byte[]> blockHashes) {
        super(blockHashes);
    }

    public NewBlockHashesMessage(byte[] payload) {
        super(payload);
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.NEW_BLOCK_HASHES;
    }
}
