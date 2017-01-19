package org.ethereum.net.par.message;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * Wrapper around Parity v1 'SnapshotData" message on the network
 *
 * @see ParMessageCodes#SNAPSHOT_DATA
 */
public class GetSnapshotDataMessage extends ParMessage {

    private byte[] chunkHash;

    public GetSnapshotDataMessage(byte[] encoded) {
        super(encoded);
    }

    public GetSnapshotDataMessage(ByteArrayWrapper chunkHash) {
        this.chunkHash = chunkHash.getData();
        encode();
    }

    private void encode() {
        byte[] chunkHash = RLP.encodeElement(this.chunkHash);

        this.encoded = RLP.encodeList(chunkHash);
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        if (paramsList.isEmpty()) {
            parsed = true;
            return;
        }

        RLPItem chunkHash = (RLPItem)  paramsList.get(0);
        this.chunkHash = chunkHash.getRLPData();

        parsed = true;
    }

    public byte[] getChunkHash() {
        return chunkHash;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public ParMessageCodes getCommand() {
        return ParMessageCodes.GET_SNAPSHOT_DATA;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return SnapshotDataMessage.class;
    }

    public String toString() {
        parse();

        return String.format("GET_SNAPSHOT_DATA [ chunkHash: %s ]", Hex.toHexString(chunkHash));
    }
}