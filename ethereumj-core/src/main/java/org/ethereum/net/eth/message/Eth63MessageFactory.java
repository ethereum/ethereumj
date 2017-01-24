package org.ethereum.net.eth.message;

import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;

import static org.ethereum.net.eth.EthVersion.V63;

/**
 * Fast synchronization (PV63) message factory
 */
public class Eth63MessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        EthMessageCodes receivedCommand = EthMessageCodes.fromByte(code, V63);
        switch (receivedCommand) {
            case STATUS:
                return new StatusMessage(encoded);
            case NEW_BLOCK_HASHES:
                return new NewBlockHashesMessage(encoded);
            case TRANSACTIONS:
                return new TransactionsMessage(encoded);
            case GET_BLOCK_HEADERS:
                return new GetBlockHeadersMessage(encoded);
            case BLOCK_HEADERS:
                return new BlockHeadersMessage(encoded);
            case GET_BLOCK_BODIES:
                return new GetBlockBodiesMessage(encoded);
            case BLOCK_BODIES:
                return new BlockBodiesMessage(encoded);
            case NEW_BLOCK:
                return new NewBlockMessage(encoded);
            case GET_NODE_DATA:
                return new GetNodeDataMessage(encoded);
            case NODE_DATA:
                return new NodeDataMessage(encoded);
            case GET_RECEIPTS:
                return new GetReceiptsMessage(encoded);
            case RECEIPTS:
                return new ReceiptsMessage(encoded);
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}
