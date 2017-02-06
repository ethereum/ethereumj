package org.ethereum.net.par.message;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Wrapper for Par Ethereum STATUS message. <br>
 *
 * @see ParMessageCodes#STATUS
 */
public class ParStatusMessage extends ParMessage {

    protected byte protocolVersion;
    protected int networkId;

    /**
     * Total difficulty of the best chain as found in block header.
     */
    protected byte[] totalDifficulty;
    /**
     * The hash of the best (i.e. highest TD) known block.
     */
    protected byte[] bestHash;
    /**
     * The hash of the Genesis block
     */
    protected byte[] genesisHash;

    protected byte[] snapshotHash;

    protected long snapshotNumber;


    public ParStatusMessage(byte[] encoded) {
        super(encoded);
    }

    public ParStatusMessage(byte protocolVersion, int networkId,
                            byte[] totalDifficulty, byte[] bestHash, byte[] genesisHash,
                            byte[] snapshotHash, long snapshotNumber) {
        this.protocolVersion = protocolVersion;
        this.networkId = networkId;
        this.totalDifficulty = totalDifficulty;
        this.bestHash = bestHash;
        this.genesisHash = genesisHash;
        this.snapshotHash = snapshotHash;
        this.snapshotNumber = snapshotNumber;

        this.parsed = true;
    }

    protected synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.protocolVersion = paramsList.get(0).getRLPData()[0];
        byte[] networkIdBytes = paramsList.get(1).getRLPData();
        this.networkId = networkIdBytes == null ? 0 : ByteUtil.byteArrayToInt(networkIdBytes);

        byte[] diff = paramsList.get(2).getRLPData();
        this.totalDifficulty = (diff == null) ? ByteUtil.ZERO_BYTE_ARRAY : diff;
        this.bestHash = paramsList.get(3).getRLPData();
        this.genesisHash = paramsList.get(4).getRLPData();

        this.snapshotHash = paramsList.get(5).getRLPData();
        byte[] snapshotNumber = paramsList.get(6).getRLPData();
        this.snapshotNumber = snapshotNumber == null ? 0 : new BigInteger(1, snapshotNumber).longValue();

        parsed = true;
    }

    protected void encode() {
        byte[] protocolVersion = RLP.encodeByte(this.protocolVersion);
        byte[] networkId = RLP.encodeInt(this.networkId);
        byte[] totalDifficulty = RLP.encodeElement(this.totalDifficulty);
        byte[] bestHash = RLP.encodeElement(this.bestHash);
        byte[] genesisHash = RLP.encodeElement(this.genesisHash);
        byte[] snapshotHash = RLP.encodeElement(this.snapshotHash);
        byte[] snapshotNumber = RLP.encodeBigInteger(BigInteger.valueOf(this.snapshotNumber));

        this.encoded = RLP.encodeList( protocolVersion, networkId,
                totalDifficulty, bestHash, genesisHash, snapshotHash, snapshotNumber);
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

    public byte getProtocolVersion() {
        parse();
        return protocolVersion;
    }

    public int getNetworkId() {
        parse();
        return networkId;
    }

    public byte[] getTotalDifficulty() {
        parse();
        return totalDifficulty;
    }

    public BigInteger getTotalDifficultyAsBigInt() {
        return new BigInteger(1, getTotalDifficulty());
    }

    public byte[] getBestHash() {
        parse();
        return bestHash;
    }

    public byte[] getGenesisHash() {
        parse();
        return genesisHash;
    }

    public byte[] getSnapshotHash() {
        return snapshotHash;
    }

    public long getSnapshotNumber() {
        return snapshotNumber;
    }

    @Override
    public ParMessageCodes getCommand() {
        return ParMessageCodes.STATUS;
    }


    @Override
    public String toString() {
        parse();
        return "[" + this.getCommand().name() +
                " protocolVersion=" + this.protocolVersion +
                " networkId=" + this.networkId +
                " totalDifficulty=" + ByteUtil.toHexString(this.totalDifficulty) +
                " bestHash=" + Hex.toHexString(this.bestHash) +
                " genesisHash=" + Hex.toHexString(this.genesisHash) +
                "]";
    }
}
