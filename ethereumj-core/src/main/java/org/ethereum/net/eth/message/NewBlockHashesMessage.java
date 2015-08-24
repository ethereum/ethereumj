package org.ethereum.net.eth.message;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum NewBlockHashes message on the network<br>
 *
 * @see EthMessageCodes#NEW_BLOCK_HASHES
 *
 * @author Mikhail Kalinin
 * @since 18.08.2015
 */
public class NewBlockHashesMessage extends EthMessage {

    /**
     * List of block hashes from the peer ordered from child to parent
     */
    private List<byte[]> blockHashes;

    public NewBlockHashesMessage(byte[] payload) {
        super(payload);
    }

    public NewBlockHashesMessage(List<byte[]> blockHashes) {
        this.blockHashes = blockHashes;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockHashes = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPItem rlpData = ((RLPItem) paramsList.get(i));
            blockHashes.add(rlpData.getRLPData());
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        encodedElements.add(RLP.encodeByte(getCommand().asByte()));
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
        return EthMessageCodes.NEW_BLOCK_HASHES;
    }

    @Override
    public String toString() {
        if (!parsed) parse();

        final String hashListShort = Utils.getHashListShort(blockHashes);
        return "[" + this.getCommand().name() + hashListShort + "] (" + blockHashes.size() + ")";
    }

}
