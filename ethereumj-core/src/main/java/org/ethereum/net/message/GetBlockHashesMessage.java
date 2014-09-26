package org.ethereum.net.message;

import java.math.BigInteger;

import static org.ethereum.net.Command.GET_BLOCK_HASHES;

import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

public class GetBlockHashesMessage extends Message {
	
    /** The hash from the block of which parent hash to start sending */
    private byte[] hash;
    /** The maximum number of blocks to return. Note: the peer could return fewer. */
    private int maxBlocks;

    public GetBlockHashesMessage(byte[] encoded) {
        super(encoded);
    }

	public GetBlockHashesMessage(byte[] hash, int maxBlocks) {
        this.hash = hash;
        this.maxBlocks = maxBlocks;
        this.parsed = true;
        this.encode();
    }

	private void encode() {
		byte[] command 		= RLP.encodeByte(this.getCommand().asByte());
		byte[] hash 		= RLP.encodeElement(this.hash);
		byte[] maxBlocks 	= RLP.encodeInt(this.maxBlocks);
		this.encoded = RLP.encodeList(command, hash, maxBlocks);
	}
	
	public void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.hash = ((RLPItem) paramsList.get(1)).getRLPData();
        byte[] maxBlocksBytes = ((RLPItem) paramsList.get(2)).getRLPData();
        this.maxBlocks		= new BigInteger(1, maxBlocksBytes).intValue();
        
        this.parsed = true;
	}

	@Override
	public byte[] getEncoded() {
		return encoded;
	}

	@Override
	public Command getCommand() {
		return GET_BLOCK_HASHES;
	}

	@Override
	public Class<BlockHashesMessage> getAnswerMessage() {
		return BlockHashesMessage.class;
	}

	public byte[] getHash() {
		if (!parsed) parse();
		return hash;
	}

	public int getMaxBlocks() {
		if (!parsed) parse();
		return maxBlocks;
	}

	@Override
	public String toString() {
		return "[command=" + this.getCommand().name() + "]"; //TODO
	}

}