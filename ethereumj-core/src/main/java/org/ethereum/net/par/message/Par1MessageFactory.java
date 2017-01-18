package org.ethereum.net.par.message;

import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.par.ParVersion;

/**
 * Warp synchronization (PAR1) message factory
 */
public class Par1MessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        ParMessageCodes receivedCommand = ParMessageCodes.fromByte(code, ParVersion.PAR1);
        switch (receivedCommand) {
            case STATUS:
                return new ParStatusMessage(encoded);
            case GET_SNAPSHOT_MANIFEST:
                return new GetSnapshotManifestMessage(encoded);
            case SNAPSHOT_MANIFEST:
                return new SnapshotManifestMessage(encoded);
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}
