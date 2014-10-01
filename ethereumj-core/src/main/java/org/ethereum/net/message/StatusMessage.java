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
        this.encode();
    }
	
    private void parse() {

		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        /* the message does not distinguish between the 0 and null
         * so check command code for null */
        // TODO: find out if it can be 00
        if ((((RLPItem)paramsList.get(0)).getRLPData()[0] & 0xFF) != STATUS.asByte())
            throw new RuntimeException("Not a StatusMessage command");
        
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
		if (encoded == null) this.encode();
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
		if (!parsed) this.parse();
		return protocolVersion;
	}

	public byte getNetworkId() {
		if (!parsed) this.parse();
		return networkId;
	}

	public byte[] getTotalDifficulty() {
		if (!parsed) this.parse();
		return totalDifficulty;
	}

	public byte[] getBestHash() {
		if (!parsed) this.parse();
		return bestHash;
	}

	public byte[] getGenesisHash() {
		if (!parsed) this.parse();
		return genesisHash;
	}
	
	@Override
	public String toString() {
		if (!parsed) parse();
		return "[" + this.getCommand().name() +
    		" protocolVersion=" + this.protocolVersion +
            " networkId=" + this.networkId +
            " totalDifficulty=" + ByteUtil.toHexString(this.totalDifficulty) +
            " bestHash=" + Hex.toHexString(this.bestHash) + " " +
            " genesisHash=" + Hex.toHexString(this.genesisHash) + " " +
            "]";
	}
}
