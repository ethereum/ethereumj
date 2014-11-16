package org.ethereum.net.eth;

import org.ethereum.core.Block;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum Blocks message on the network
 * 
 * @see org.ethereum.net.eth.EthMessageCodes#NEW_BLOCK
 */
public class NewBlockMessage extends EthMessage {

	private Block block;
    private byte[] difficulty;

	public NewBlockMessage(byte[] encoded) {
		super(encoded);
	}

    public NewBlockMessage(Block block, byte[] difficulty){
        this.block = block;
        this.difficulty = difficulty;
        encode();
    }

    private void encode(){
        byte[] command = RLP.encodeByte( this.getCommand().asByte());
        byte[] block = this.block.getEncoded();
        byte[] diff  = RLP.encodeElement(this.difficulty);

        this.encoded = RLP.encodeList(command, block, diff);
        parsed = true;
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
        return "NEW_BLOCK [ " + blockString + "\n difficulty: " + Hex.toHexString(difficulty) + " ]";
	}
}