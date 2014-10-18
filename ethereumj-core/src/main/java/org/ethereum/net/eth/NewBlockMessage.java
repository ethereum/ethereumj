package org.ethereum.net.eth;

import org.ethereum.core.Block;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

/**
 * Wrapper around an Ethereum Blocks message on the network
 * 
 * @see {@link org.ethereum.net.eth.EthMessageCodes#NEW_BLOCK}
 */
public class NewBlockMessage extends EthMessage {

	private Block block;
    private byte[] difficulty;

	public NewBlockMessage(byte[] encoded) {
		super(encoded);
	}

	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        RLPList blockRLP = ((RLPList) paramsList.get(1));
        block = new Block(blockRLP.getRLPData());
        difficulty =  paramsList.get(2).getRLPData();

        parsed = true;
	}

    public Block getBlock(){
        if (!parsed) parse();
        return block;
    }
    
    public byte[] getDifficulty(){
        if (!parsed) parse();
        return difficulty;
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

        String blockString = this.getBlock().toString();
        return "NEW_BLOCK [ " + blockString + " ]";
	}
}