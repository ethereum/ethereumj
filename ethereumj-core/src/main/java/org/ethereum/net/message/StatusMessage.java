package org.ethereum.net.message;

import static org.ethereum.net.message.Command.STATUS;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum Status message on the network 
 *
 * @see {@link org.ethereum.net.message.Command#STATUS}
 */
public class StatusMessage extends Message {

    private byte protocolVersion;
    private byte networkId;
    
    /** Total difficulty of the best chain as found in block header. */
    private byte[] totalDifficulty;
    /** The hash of the best (i.e. highest TD) known block. */
    private byte[] bestHash;
    /** The hash of the Genesis block */
    private byte[] genesisHash;

    public StatusMessage(byte[] encoded) {
        super(encoded);
    }

	public StatusMessage(byte protocolVersion, byte networkId, 
			byte[] totalDifficulty, byte[] bestHash, byte[] genesisHash) {
        this.protocolVersion = protocolVersion;
        this.networkId = networkId;
        this.totalDifficulty = totalDifficulty;
        this.bestHash = bestHash;
        this.genesisHash = genesisHash;
        this.parsed = true;
    }
	
    private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
		validateMessage(paramsList, STATUS);
        
        this.protocolVersion	= ((RLPItem) paramsList.get(1)).getRLPData()[0];
        byte[] networkIdBytes	= ((RLPItem) paramsList.get(2)).getRLPData();
        this.networkId			= networkIdBytes == null ? 0 : networkIdBytes[0];
        this.totalDifficulty	= ((RLPItem) paramsList.get(3)).getRLPData();       
        this.bestHash 			= ((RLPItem) paramsList.get(4)).getRLPData();
        this.genesisHash 		= ((RLPItem) paramsList.get(5)).getRLPData();
        
        parsed = true;
    }

    private void encode() {
        byte[] command			= RLP.encodeByte(STATUS.asByte());
        byte[] protocolVersion	= RLP.encodeByte(this.protocolVersion);
        byte[] networkId		= RLP.encodeByte(this.networkId);
        byte[] totalDifficulty	= RLP.encodeElement(this.totalDifficulty);
        byte[] bestHash			= RLP.encodeElement(this.bestHash);
        byte[] genesisHash		= RLP.encodeElement(this.genesisHash);

		this.encoded = RLP.encodeList(command, protocolVersion, networkId,
				totalDifficulty, bestHash, genesisHash);
	}

	@Override
	public byte[] getEncoded() {
		if (encoded == null) encode();
        return encoded;
	}
	
	@Override
	public Command getCommand() {
		return STATUS;
	}

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}

	public byte getProtocolVersion() {
		if (!parsed) parse();
		return protocolVersion;
	}

	public byte getNetworkId() {
		if (!parsed) parse();
		return networkId;
	}

	public byte[] getTotalDifficulty() {
		if (!parsed) parse();
		return totalDifficulty;
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
