package org.ethereum.net.message;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.Command.BLOCKS;

import org.ethereum.core.Block;
import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class BlocksMessage extends Message {

	private List<Block> blockDataList = new ArrayList<>();

    public BlocksMessage(byte[] encoded) {
        super(encoded);
    }

	public void parse() {

		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
		
        if ( (((RLPItem)(paramsList).get(0)).getRLPData()[0] & 0xFF) != BLOCKS.asByte())
            throw new RuntimeException("Not a BlocksMessage command");

		for (int i = 1; i < paramsList.size(); ++i) {
			RLPList rlpData = ((RLPList) paramsList.get(i));
			Block blockData = new Block(rlpData.getRLPData());
			this.blockDataList.add(blockData);
		}
		parsed = true;
	}
	
	@Override
	public Command getCommand() {
		return BLOCKS;
	}

	@Override
	public byte[] getEncoded() {
		return encoded;
	}

	public List<Block> getBlockDataList() {
		if (!parsed) parse();
		return blockDataList;
	}

	@Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        if (!parsed) parse();

		StringBuffer sb = new StringBuffer();
		for (Block blockData : this.getBlockDataList()) {
			sb.append("   ").append(blockData.toFlatString()).append("\n");
		}
		return "[command=" + getCommand().name() + "\n" + sb.toString() + " ]";
	}
}