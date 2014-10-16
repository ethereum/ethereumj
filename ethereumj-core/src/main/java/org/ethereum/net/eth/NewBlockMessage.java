package org.ethereum.net.eth;

import org.ethereum.core.Block;
import org.ethereum.net.eth.EthMessage;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.eth.EthMessageCodes.NEW_BLOCK;

/**
 * Wrapper around an Ethereum Blocks message on the network
 */
public class NewBlockMessage extends EthMessage {

	private Block block;
    private byte[] difficulty;

	public NewBlockMessage(byte[] encoded) {
		super(encoded);
	}

	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        RLPItem blockItem = ((RLPItem) paramsList.get(0));
        block = new Block(blockItem.getRLPData());

        difficulty = ((RLPItem) paramsList.get(1)).getRLPData();

        parsed = true;
	}

	@Override
	public byte[] getEncoded() {
		return encoded;
	}

    @Override
    public EthMessageCodes getCommand(){
        return EthMessageCodes.NEW_BLOCK;
    }

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}

	public String toString() {
		if (!parsed) parse();

        return "";
	}
}