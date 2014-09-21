package org.ethereum.net.message;

public class GetBlockHashesMessage extends Message {

	@Override
	public void parseRLP() {
	}

	@Override
	public byte[] getPayload() {
		return null;
	}

	@Override
	public String getMessageName() {
		return "GetBlockHashes";
	}

	@Override
	public Class<BlockHashesMessage> getAnswerMessage() {
		return BlockHashesMessage.class;
	}

}
