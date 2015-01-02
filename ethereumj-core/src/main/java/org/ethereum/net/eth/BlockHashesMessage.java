package org.ethereum.net.eth;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.eth.EthMessageCodes.BLOCK_HASHES;

/**
 * Wrapper around an Ethereum BlockHashes message on the network
 *
 * @see org.ethereum.net.eth.EthMessageCodes#BLOCK_HASHES
 */
public class BlockHashesMessage extends EthMessage {

    /**
     * List of block hashes from the peer ordered from child to parent
     */
    private List<byte[]> blockHashes;

    public BlockHashesMessage(byte[] payload) {
        super(payload);
    }

    public BlockHashesMessage(List<byte[]> blockHashes) {
        this.blockHashes = blockHashes;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockHashes = new ArrayList<>();
        for (int i = 1; i < paramsList.size(); ++i) {
            RLPItem rlpData = ((RLPItem) paramsList.get(i));
            blockHashes.add(rlpData.getRLPData());
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        encodedElements.add(RLP.encodeByte(BLOCK_HASHES.asByte()));
        for (byte[] blockHash : blockHashes)
            encodedElements.add(RLP.encodeElement(blockHash));
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

    public List<byte[]> getBlockHashes() {
        if (!parsed) parse();
        return blockHashes;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.BLOCK_HASHES;
    }

    @Override
    public String toString() {
        if (!parsed) parse();

        StringBuffer sb = Utils.getHashlistShort(this.blockHashes);
        return "[" + this.getCommand().name() + sb.toString() + "] (" + this.blockHashes.size() + ")";
    }
}
