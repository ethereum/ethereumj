package org.ethereum.net.swarm.bzz;

import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class BzzMessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        BzzMessageCodes receivedCommand = BzzMessageCodes.fromByte(code);
        switch (receivedCommand) {
            case STATUS:
                return new BzzStatusMessage(encoded);
            case STORE_REQUEST:
                return new BzzStoreReqMessage(encoded);
            case RETRIEVE_REQUEST:
                return new BzzRetrieveReqMessage(encoded);
            case PEERS:
                return new BzzPeersMessage(encoded);
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}
