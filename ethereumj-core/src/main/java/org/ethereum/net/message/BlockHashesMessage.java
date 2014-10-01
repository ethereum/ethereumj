package org.ethereum.net.message;

import static org.ethereum.net.message.Command.BLOCK_HASHES;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around an Ethereum BlockHashes message on the network 
 *
 * @see {@link org.ethereum.net.message.Command#BLOCK_HASHES}
 */
public class BlockHashesMessage extends Message {

	private List<byte[]> hashes;
	
	public BlockHashesMessage(byte[] payload) {
		super(payload);
	}

	public BlockHashesMessage(List<byte[]> blockHashes) {
		this.hashes = blockHashes;
		parsed = true;
	}
	
	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
		
        if ((((RLPItem)paramsList.get(0)).getRLPData()[0] & 0xFF) != BLOCK_HASHES.asByte())
            throw new RuntimeException("Not a BlockHashesMessage command");

        hashes = new ArrayList<>();
		for (int i = 1; i < paramsList.size(); ++i) {
			RLPItem rlpData = ((RLPItem) paramsList.get(i));
			hashes.add(rlpData.getRLPData());
		}
		parsed = true;
	}
	
	private void encode() {
    	List<byte[]> encodedElements = new ArrayList<>();
    	encodedElements.add(RLP.encodeByte(BLOCK_HASHES.asByte()));
    	for (byte[] hash : hashes)
            encodedElements.add(RLP.encodeElement(hash));
		byte[][] encodedElementArray = encodedElements
				.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
	}
	
	@Override
	public Command getCommand() {
		return BLOCK_HASHES;
	}
	
	@Override
	public byte[] getEncoded() {
		if (encoded == null) this.encode();
		return encoded;
	}

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}
	
	public List<byte[]> getHashes() {
		if(!parsed) parse();
		return hashes;
	}

	@Override
    public String toString() {
        if (!parsed) parse();
		StringBuffer sb = new StringBuffer();
		for (byte[] hash : this.hashes) {
			sb.append("\n   ").append(Hex.toHexString(hash));
		}
        return "[" + this.getCommand().name() + sb.toString() + "]";
    }
}
