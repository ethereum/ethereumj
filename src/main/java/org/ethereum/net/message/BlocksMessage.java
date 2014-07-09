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

	private List<Block> blockDataList = new ArrayList<Block>();

	public BlocksMessage(RLPList rawData) {
		super(rawData);
	}

    public BlocksMessage(byte[] payload) {
        super(RLP.decode2(payload));
        this.payload = payload;
    }


	public void parseRLP() {

		RLPList paramsList = (RLPList) rawData.get(0);
		
		if (Command.fromInt(((RLPItem) (paramsList).get(0)).getRLPData()[0]) != BLOCKS) {
			throw new Error("BlocksMessage: parsing for mal data");
		}

		for (int i = 1; i < paramsList.size(); ++i) {
			RLPList rlpData = ((RLPList) paramsList.get(i));
			Block blockData = new Block(rlpData.getRLPData());
			this.blockDataList.add(blockData);
		}
		parsed = true;
	}

	@Override
	public byte[] getPayload() {
		return payload;
	}

	public List<Block> getBlockDataList() {
		if (!parsed) parseRLP();
		return blockDataList;
	}

    @Override
    public String getMessageName() {
        return "Block";
    }

    @Override
    public Class getAnswerMessage() {
        return null;
    }

    public String toString() {
        if (!parsed) parseRLP();

		StringBuffer sb = new StringBuffer();
		for (Block blockData : this.getBlockDataList()) {
			sb.append("   ").append(blockData.toFlatString()).append("\n");

		}
		return "Blocks Message [\n" + sb.toString() + " ]";
	}
}