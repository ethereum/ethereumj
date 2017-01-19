package org.ethereum.net.par.message;

import org.ethereum.util.RLP;

/**
 * Wrapper around Parity v1 'GetSnapshotManifest" message on the network
 *
 * @see ParMessageCodes#GET_SNAPSHOT_MANIFEST
 */
public class GetSnapshotManifestMessage extends ParMessage {

    public GetSnapshotManifestMessage() {
        super();
        parsed = true;
    }

    public GetSnapshotManifestMessage(byte[] encoded) {
        this();
    }

    @Override
    public byte[] getEncoded() {
         return RLP.encodeList();
    }

    @Override
    public Class<SnapshotManifestMessage> getAnswerMessage() {
        return SnapshotManifestMessage.class;
    }

    @Override
    public ParMessageCodes getCommand() {
        return ParMessageCodes.GET_SNAPSHOT_MANIFEST;
    }

    public String toString() {
        return "[" + getCommand().name() + "]";
    }
}
