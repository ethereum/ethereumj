package org.ethereum.net.eth.message;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum BlockHeaders message on the network
 *
 * @see EthMessageCodes#BLOCK_HEADERS
 *
 * @author Mikhail Kalinin
 * @since 04.09.2015
 */
public class BlockHeadersMessage extends EthMessage {

    /**
     * List of block headers from the peer
     */
    private List<BlockHeader> blockHeaders;

    public BlockHeadersMessage(byte[] encoded) {
        super(encoded);
    }

    public BlockHeadersMessage(List<Block> blocks) {
        this.blockHeaders = new ArrayList<>(blocks.size());
        for (Block b : blocks) {
            this.blockHeaders.add(b.getHeader());
        }
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockHeaders = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpData = ((RLPList) paramsList.get(i));
            blockHeaders.add(new BlockHeader(rlpData));
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (BlockHeader blockHeader : blockHeaders)
            encodedElements.add(blockHeader.getEncoded());
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }


    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public List<BlockHeader> getBlockHeaders() {
        if (!parsed) parse();
        return blockHeaders;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.BLOCK_HEADERS;
    }

    @Override
    public String toString() {
        if (!parsed) parse();

        return "[" + this.getCommand().name() + "] (" + blockHeaders.size() + ")";
    }
}
