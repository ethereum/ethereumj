package org.ethereum.net.eth.message;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an Ethereum GetBlockBodies message on the network
 *
 * @see EthMessageCodes#GET_BLOCK_BODIES
 *
 * @author Mikhail Kalinin
 * @since 04.09.2015
 */
public class GetBlockBodiesMessage extends EthMessage {

    /**
     * List of block hashes for which to retrieve the block bodies
     */
    private List<byte[]> blockHashes;

    public GetBlockBodiesMessage(byte[] encoded) {
        super(encoded);
    }

    public GetBlockBodiesMessage(List<byte[]> blockHashes) {
        this.blockHashes = blockHashes;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockHashes = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            blockHashes.add(paramsList.get(i).getRLPData());
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (byte[] hash : blockHashes)
            encodedElements.add(RLP.encodeElement(hash));
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    @Override
    public Class<BlockBodiesMessage> getAnswerMessage() {
        return BlockBodiesMessage.class;
    }

    public List<byte[]> getBlockHashes() {
        if (!parsed) parse();
        return blockHashes;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.GET_BLOCK_BODIES;
    }

    public String toString() {
        final String hashListShort = Utils.getHashListShort(getBlockHashes());
        return "[" + this.getCommand().name() + hashListShort + "] (" + blockHashes.size() + ")";
    }
}
