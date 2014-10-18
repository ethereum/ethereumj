package org.ethereum.net.eth;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.core.Block;
import org.ethereum.net.eth.EthMessage;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

/**
 * Wrapper around an Ethereum Blocks message on the network
 *
 * @see {@link org.ethereum.net.eth.EthMessageCodes#BLOCKS}
 */
public class BlocksMessage extends EthMessage {

	private List<Block> blocks;

	public BlocksMessage(byte[] encoded) {
		super(encoded);
	}

	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

		blocks = new ArrayList<>();
		for (int i = 1; i < paramsList.size(); ++i) {
			RLPList rlpData = ((RLPList) paramsList.get(i));
			Block blockData = new Block(rlpData.getRLPData());
			blocks.add(blockData);
		}
		parsed = true;
	}

	@Override
	public byte[] getEncoded() {
		return encoded;
	}

	public List<Block> getBlocks() {
		if (!parsed) parse();
		return blocks;
	}

    @Override
    public EthMessageCodes getCommand(){
        return EthMessageCodes.BLOCKS;
    }

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}

	public String toString() {
		if (!parsed) parse();

		StringBuffer sb = new StringBuffer();
		for (Block blockData : this.getBlocks()) {
			sb.append("\n   ").append(blockData.toFlatString());
		}
		return "[" + getCommand().name() + sb.toString() + "]";
	}
}