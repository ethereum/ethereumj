package org.ethereum.net.message;

import static org.ethereum.net.Command.BLOCK_HASHES;

import java.util.List;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.Command;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import com.google.common.base.Joiner;

public class BlockHashesMessage extends Message {

	private List<ByteArrayWrapper> hashes;
	
	public BlockHashesMessage(byte[] payload) {
		super(payload);
	}
	
	private void parse() {
		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
		this.encoded = new byte[0]; // TODO
	}
	
	@Override
	public Command getCommand() {
		return BLOCK_HASHES;
	}
	
	@Override
	public byte[] getEncoded() {
		return encoded;
	}

	@Override
	public Class<?> getAnswerMessage() {
		return null;
	}
	
	@Override
    public String toString() {
        if (!parsed) parse();
        return "[command=" + this.getCommand().name() + " hashes=" + Joiner.on("\n").join(hashes) + "]";
    }
}
