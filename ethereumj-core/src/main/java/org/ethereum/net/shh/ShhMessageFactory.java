package org.ethereum.net.shh;

import org.ethereum.net.message.*;
import org.ethereum.net.message.Message;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class ShhMessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        ShhMessageCodes receivedCommand = ShhMessageCodes.fromByte(code);
        switch (receivedCommand) {
            case STATUS:
                return new StatusMessage(encoded);
            case MESSAGE:
                return new Envelope(encoded);
            case ADD_FILTER:
            case REMOVE_FILTER:
            case PACKET_COUNT:
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}
