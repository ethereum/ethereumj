package org.ethereum.net.message;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.message.Command.BLOCKS;

import org.ethereum.core.Block;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

/**
 * Wrapper around an Ethereum Blocks message on the network
 *
 * @see {@link org.ethereum.net.message.Command#BLOCKS}
 */
public class BlocksMessage extends Message {

	private List<Block> blocks;

	public BlocksMessage(byte[] encoded) {
		super(encoded);
	}

	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
		validateMessage(paramsList, BLOCKS);

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

	@Override
	public Command getCommand() {
		return BLOCKS;
	}

	public List<Block> getBlocks() {
		if (!parsed) parse();
		return blocks;
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