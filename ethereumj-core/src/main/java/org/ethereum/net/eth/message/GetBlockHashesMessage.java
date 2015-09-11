package org.ethereum.net.eth.message;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum GetBlockHashes message on the network
 *
 * @see EthMessageCodes#GET_BLOCK_HASHES
 */
public class GetBlockHashesMessage extends EthMessage {

    /**
     * The newest block hash from which to start sending older hashes
     */
    private byte[] bestHash;

    /**
     * The maximum number of blocks to return.
     * <b>Note:</b> the peer could return fewer.
     */
    private int maxBlocks;

    public GetBlockHashesMessage(byte[] encoded) {
        super(encoded);
    }

    public GetBlockHashesMessage(byte[] hash, int maxBlocks) {
        this.bestHash = hash;
        this.maxBlocks = maxBlocks;
        parsed = true;
        encode();
    }

    private void encode() {
        byte[] hash = RLP.encodeElement(this.bestHash);
        byte[] maxBlocks = RLP.encodeInt(this.maxBlocks);
        this.encoded = RLP.encodeList(hash, maxBlocks);
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.bestHash = paramsList.get(0).getRLPData();
        byte[] maxBlocksBytes = paramsList.get(1).getRLPData();
        this.maxBlocks = ByteUtil.byteArrayToInt(maxBlocksBytes);

        parsed = true;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    @Override
    public Class<BlockHashesMessage> getAnswerMessage() {
        return BlockHashesMessage.class;
    }

    public byte[] getBestHash() {
        if (!parsed) parse();
        return bestHash;
    }

    public int getMaxBlocks() {
        if (!parsed) parse();
        return maxBlocks;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.GET_BLOCK_HASHES;
    }


    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
                " bestHash=" + Hex.toHexString(bestHash) +
                " maxBlocks=" + maxBlocks + "]";
    }
}