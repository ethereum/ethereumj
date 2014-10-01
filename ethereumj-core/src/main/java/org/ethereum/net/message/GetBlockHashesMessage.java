package org.ethereum.net.message;

import static org.ethereum.net.message.Command.GET_BLOCK_HASHES;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum GetBlockHashes message on the network
 *
 * @see {@link org.ethereum.net.message.Command#GET_BLOCK_HASHES}
 */
public class GetBlockHashesMessage extends Message {

	/** The hash from the block of which parent hash to start sending */
	private byte[] hash;
	
	/** The maximum number of blocks to return. 
	 * <b>Note:</b> the peer could return fewer. */
	private int maxBlocks;

	public GetBlockHashesMessage(byte[] encoded) {
		super(encoded);
	}

	public GetBlockHashesMessage(byte[] hash, int maxBlocks) {
		this.hash = hash;
		this.maxBlocks = maxBlocks;
		parsed = true;
		encode();
	}

	private void encode() {
		byte[] command = RLP.encodeByte(GET_BLOCK_HASHES.asByte());
		byte[] hash = RLP.encodeElement(this.hash);
		byte[] maxBlocks = RLP.encodeInt(this.maxBlocks);
		this.encoded = RLP.encodeList(command, hash, maxBlocks);
	}

	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
		validateMessage(paramsList, GET_BLOCK_HASHES);

		this.hash = ((RLPItem) paramsList.get(1)).getRLPData();
		byte[] maxBlocksBytes = ((RLPItem) paramsList.get(2)).getRLPData();
		this.maxBlocks = ByteUtil.byteArrayToInt(maxBlocksBytes);

		parsed = true;
	}

	@Override
	public byte[] getEncoded() {
		if(encoded == null) encode();
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
		if (!parsed) parse();
		return "[" + this.getCommand().name() + 
				" hash=" + Hex.toHexString(hash) + 
				" maxBlocks=" + maxBlocks + "]";
	}
}