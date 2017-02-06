package org.ethereum.net.par.message;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around Parity v1 'SnapshotData" message on the network
 *
 * @see ParMessageCodes#SNAPSHOT_DATA
 */
public class SnapshotDataMessage extends ParMessage {

    private RLPElement chunkData;

    public SnapshotDataMessage(byte[] encoded) {
        super(encoded);
    }
// TODO
//    public SnapshotManifestMessage(Block block, byte[] difficulty) {
//        this.block = block;
//        this.difficulty = difficulty;
//        this.parsed = true;
//        encode();
//    }
//
//    private void encode() {
//        byte[] block = this.block.getEncoded();
//        byte[] diff = RLP.encodeElement(this.difficulty);
//
//        this.encoded = RLP.encodeList(block, diff);
//    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        if (paramsList.isEmpty()) {
            parsed = true;
            return;
        }

        this.chunkData = paramsList.get(0);
        parsed = true;
    }

    public RLPElement getChunkData() {
        if (!parsed) parse();
        return chunkData;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public ParMessageCodes getCommand() {
        return ParMessageCodes.SNAPSHOT_DATA;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        return String.format("SNAPSHOT_DATA [ %d bytes ]", encoded.length);
    }
}