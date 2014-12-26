package org.ethereum.net.message;

import org.ethereum.net.eth.*;
import org.ethereum.net.p2p.*;
import org.ethereum.net.shh.ShhMessageCodes;
import org.ethereum.util.RLP;

/**
 * Factory to create protocol message objects based on the RLP encoded data
 */
public class MessageFactory {

    public static Message createMessage(byte[] encoded) {
        byte code = RLP.getCommandCode(encoded);

        if (P2pMessageCodes.inRange(code)){

            P2pMessageCodes receivedCommand = P2pMessageCodes.fromByte(code);
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
            }
        }

        if (EthMessageCodes.inRange(code)) {

            EthMessageCodes receivedCommand = EthMessageCodes.fromByte(code);
            switch (receivedCommand) {
                case STATUS:
                    return new StatusMessage(encoded);
                case GET_TRANSACTIONS:
                    return StaticMessages.GET_TRANSACTIONS_MESSAGE;
                case TRANSACTIONS:
                    return new TransactionsMessage(encoded);
                case GET_BLOCK_HASHES:
                    return new GetBlockHashesMessage(encoded);
                case BLOCK_HASHES:
                    return new BlockHashesMessage(encoded);
                case GET_BLOCKS:
                    return new GetBlocksMessage(encoded);
                case BLOCKS:
                    return new BlocksMessage(encoded);
                case NEW_BLOCK:
                    return new NewBlockMessage(encoded);
                case PACKET_COUNT:
                    return new PacketCountMessage(encoded);
            }
        }

        if (ShhMessageCodes.inRange(code)) {

            ShhMessageCodes receivedCommand = ShhMessageCodes.fromByte(code);
            switch (receivedCommand) {
                case STATUS:
                    break;
                case MESSAGE:
                    break;
                case ADD_FILTER:
                    break;
                case REMOVE_FILTER:
                    break;
                case PACKET_COUNT:
                    break;
            }
        }

        throw new IllegalArgumentException("No such message");
    }

}
