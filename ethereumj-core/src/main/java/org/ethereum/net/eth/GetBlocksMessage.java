package org.ethereum.net.eth;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;

import static org.ethereum.net.eth.EthMessageCodes.GET_BLOCKS;

/**
 * Wrapper around an Ethereum GetBlocks message on the network
 * 
 * @see {@link org.ethereum.net.eth.EthMessageCodes#GET_BLOCKS}
 */
public class GetBlocksMessage extends EthMessage {

	/** List of block hashes for which to retrieve the blocks */
	private List<byte[]> blockHashes;

	public GetBlocksMessage(byte[] encoded) {
		super(encoded);
	}

	public GetBlocksMessage(List<byte[]> blockHashes) {
		this.blockHashes = blockHashes;
		parsed = true;
	}

	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

		blockHashes = new ArrayList<>();
		for (int i = 1; i < paramsList.size(); ++i) {
			blockHashes.add(((RLPItem) paramsList.get(i)).getRLPData());
		}
		parsed = true;
	}

	private void encode() {
		List<byte[]> encodedElements = new ArrayList<>();
		encodedElements.add(RLP.encodeByte(GET_BLOCKS.asByte()));
		for (byte[] hash : blockHashes)
			encodedElements.add(RLP.encodeElement(hash));
		byte[][] encodedElementArray = encodedElements
				.toArray(new byte[encodedElements.size()][]);
		this.encoded = RLP.encodeList(encodedElementArray);
	}

	@Override
	public byte[] getEncoded() {
		if (encoded == null) encode();
		return encoded;
	}


	@Override
	public Class<BlocksMessage> getAnswerMessage() {
		return BlocksMessage.class;
	}

	public List<byte[]> getBlockHashes() {
		if (!parsed) parse();
		return blockHashes;
	}

    @Override
    public EthMessageCodes getCommand(){
        return EthMessageCodes.GET_BLOCKS;
    }

	public String toString() {
		if (!parsed) parse();

		StringBuffer sb = Utils.getHashlistShort(this.blockHashes);
		return "[" + this.getCommand().name() + sb.toString() + "] (" + this.blockHashes.size() + ")";
	}
}
