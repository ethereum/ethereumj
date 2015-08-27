package org.ethereum.net.eth.message;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Wrapper around an Ethereum GetBlockHashesByNumber message on the network. <br>
 *
 * @see EthMessageCodes#GET_BLOCK_HASHES_BY_NUMBER
 *
 * @author Mikhail Kalinin
 * @since 18.08.2015
 */
public class GetBlockHashesByNumberMessage extends EthMessage {

    /**
     * The number of block from which to start sending hashes.
     */
    private long blockNumber;

    /**
     * The maximum number of blocks to return.
     * <b>Note:</b> the peer could return fewer.
     */
    private int maxBlocks;

    public GetBlockHashesByNumberMessage(byte[] encoded) {
        super(encoded);
    }

    public GetBlockHashesByNumberMessage(long blockNumber, int maxBlocks) {
        this.blockNumber = blockNumber;
        this.maxBlocks = maxBlocks;
        parsed = true;
        encode();
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        byte[] numberBytes = paramsList.get(0).getRLPData();
        byte[] maxBlocksBytes = paramsList.get(1).getRLPData();
        this.blockNumber = numberBytes == null ? 0 : new BigInteger(1, numberBytes).longValue();
        this.maxBlocks = ByteUtil.byteArrayToInt(maxBlocksBytes);

        parsed = true;
    }

    private void encode() {
        byte[] blockNumber = RLP.encodeBigInteger(BigInteger.valueOf(this.blockNumber));
        byte[] maxBlocks = RLP.encodeInt(this.maxBlocks);
        this.encoded = RLP.encodeList(blockNumber, maxBlocks);
    }

    public long getBlockNumber() {
        if (!parsed) parse();
        return blockNumber;
    }

    public int getMaxBlocks() {
        if (!parsed) parse();
        return maxBlocks;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return BlockHashesMessage.class;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.GET_BLOCK_HASHES_BY_NUMBER;
    }

    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
                " blockNumber=" + blockNumber +
                " maxBlocks=" + maxBlocks + "]";
    }
}
