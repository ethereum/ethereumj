package org.ethereum.net.eth.message;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Ethereum Status message parent class<br>
 * Holds stuff which is common to all supported protocols
 *
 * @see EthMessageCodes#STATUS
 */
public abstract class StatusMessage extends EthMessage {

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

    protected StatusMessage(byte[] encoded) {
        super(encoded);
    }

    protected StatusMessage(byte protocolVersion, int networkId,
                         byte[] totalDifficulty, byte[] bestHash, byte[] genesisHash) {
        this.protocolVersion = protocolVersion;
        this.networkId = networkId;
        this.totalDifficulty = totalDifficulty;
        this.bestHash = bestHash;
        this.genesisHash = genesisHash;
        this.parsed = true;
    }

    abstract protected void parse();

    abstract protected void encode();

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
        if (!parsed) parse();
        return protocolVersion;
    }

    public int getNetworkId() {
        if (!parsed) parse();
        return networkId;
    }

    public byte[] getTotalDifficulty() {
        if (!parsed) parse();
        return totalDifficulty;
    }

    public BigInteger getTotalDifficultyAsBigInt() {
        return new BigInteger(1, getTotalDifficulty());
    }

    public byte[] getBestHash() {
        if (!parsed) parse();
        return bestHash;
    }

    public byte[] getGenesisHash() {
        if (!parsed) parse();
        return genesisHash;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.STATUS;
    }


    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
                " protocolVersion=" + this.protocolVersion +
                " networkId=" + this.networkId +
                " totalDifficulty=" + ByteUtil.toHexString(this.totalDifficulty) +
                " bestHash=" + Hex.toHexString(this.bestHash) +
                " genesisHash=" + Hex.toHexString(this.genesisHash) +
                "]";
    }
}
