package org.ethereum.net.message;

import org.ethereum.util.RLP;

/**
 * Factory to create protocol message objects based on the RLP encoded data
 */
public class MessageFactory {

	public static Message createMessage(byte[] encoded) {
		Command receivedCommand = Command.fromInt(RLP.getCommandCode(encoded));
		
		switch (receivedCommand) {
			case HELLO:
				return new HelloMessage(encoded);
			case DISCONNECT:
				return new DisconnectMessage(encoded);
			case PING:
				return StaticMessages.PING_MESSAGE;
			case PONG:
				return StaticMessages.PONG_MESSAGE;
			case GET_PEERS:
				return StaticMessages.GET_PEERS_MESSAGE;
			case PEERS:
				return new PeersMessage(encoded);			
		
			case STATUS:
				return new StatusMessage(encoded);
			case TRANSACTIONS:
				return new TransactionsMessage(encoded);
			case BLOCKS:
				return new BlocksMessage(encoded);
			case GET_TRANSACTIONS:
				return StaticMessages.GET_TRANSACTIONS_MESSAGE;
			case GET_BLOCK_HASHES:
				return new GetBlockHashesMessage(encoded);
			case BLOCK_HASHES:
				return new BlockHashesMessage(encoded);
			case GET_BLOCKS:
				return new GetBlocksMessage(encoded);
			default:
				throw new IllegalArgumentException("No such message");
		}
	}

}
