package org.ethereum.net.message;

import static org.ethereum.net.Command.STATUS;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

/**
 * Wrapper around an Ethereum StatusMessage on the network 
 *
 * @see {@link org.ethereum.net.Command#STATUS}
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

    public StatusMessage(RLPList rawData) {
        super(rawData);
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
	
    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.get(0);

        /* the message does not distinguish between the 0 and null
         * so check command code for null */
        // TODO: find out if it can be 00
		if (((RLPItem) paramsList.get(0)).getRLPData() != null)
            throw new Error("StaticMessage: parsing for mal data");
        
        this.protocolVersion	= ((RLPItem) paramsList.get(1)).getRLPData()[0];
        byte[] networkIdBytes	= ((RLPItem) paramsList.get(2)).getRLPData();
        this.networkId			= networkIdBytes == null ? 0 : networkIdBytes[0];
        this.totalDifficulty	= ((RLPItem) paramsList.get(3)).getRLPData();       
        this.bestHash 			= ((RLPItem) paramsList.get(4)).getRLPData();
        this.genesisHash 		= ((RLPItem) paramsList.get(5)).getRLPData();
        
        this.parsed = true;
    }

	@Override
	public byte[] getPayload() {
        byte[] command			= RLP.encodeByte(STATUS.asByte());
        byte[] protocolVersion	= RLP.encodeByte(this.protocolVersion);
        byte[] networkId		= RLP.encodeByte(this.networkId);
        byte[] totalDifficulty	= RLP.encodeElement(this.totalDifficulty);
        byte[] bestHash			= RLP.encodeElement(this.bestHash);
        byte[] genesisHash		= RLP.encodeElement(this.genesisHash);

		byte[] data = RLP.encodeList(command, protocolVersion, networkId,
				totalDifficulty, bestHash, genesisHash);

        return data;
	}

	@Override
	public String getMessageName() {
		return "StatusMessage";
	}

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}
}
