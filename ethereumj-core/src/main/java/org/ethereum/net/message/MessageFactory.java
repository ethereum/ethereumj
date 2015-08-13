package org.ethereum.net.message;

import org.ethereum.net.eth.message.*;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.PeersMessage;
import org.ethereum.net.rlpx.MessageCodesResolver;
import org.ethereum.net.shh.Envelope;
import org.ethereum.net.shh.ShhMessageCodes;
import org.ethereum.net.swarm.bzz.*;

/**
 * Factory to create protocol message objects based on the RLP encoded data
 */
public class MessageFactory {

    public static Message createMessage(byte code, byte[] encoded, MessageCodesResolver codesResolver) {

        byte resolved = codesResolver.resolveP2p(code);
        if (P2pMessageCodes.inRange(resolved)) {

            P2pMessageCodes receivedCommand = P2pMessageCodes.fromByte(resolved);
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

        resolved = codesResolver.resolveEth(code);
        if (EthMessageCodes.inRange(resolved)) {

            EthMessageCodes receivedCommand = EthMessageCodes.fromByte(resolved);
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
            }
        }

        resolved = codesResolver.resolveShh(code);
        if (ShhMessageCodes.inRange(resolved)) {

            ShhMessageCodes receivedCommand = ShhMessageCodes.fromByte(resolved);
            switch (receivedCommand) {
                case STATUS:
                    return new org.ethereum.net.shh.StatusMessage(encoded);
                case MESSAGE:
                    return new Envelope(encoded);
                case ADD_FILTER:
                    break;
                case REMOVE_FILTER:
                    break;
                case PACKET_COUNT:
                    break;
            }
        }

        resolved = codesResolver.resolveBzz(code);
        if (BzzMessageCodes.inRange(resolved)) {

            BzzMessageCodes receivedCommand = BzzMessageCodes.fromByte(resolved);
            switch (receivedCommand) {
                case STATUS:
                    return new BzzStatusMessage(encoded);
                case STORE_REQUEST:
                    return new BzzStoreReqMessage(encoded);
                case RETRIEVE_REQUEST:
                    return new BzzRetrieveReqMessage(encoded);
                case PEERS:
                    return new BzzPeersMessage(encoded);
            }
        }

        throw new IllegalArgumentException("No such message");
    }
}
