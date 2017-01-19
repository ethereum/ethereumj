package org.ethereum.net.par.message;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around Parity v1 'SnapshotManifest" message on the network
 *
 * @see ParMessageCodes#SNAPSHOT_MANIFEST
 */
public class SnapshotManifestMessage extends ParMessage {

    private List<byte[]> stateHashes;
    private List<byte[]> blockHashes;
    private byte[] stateRoot;
    private Long blockNumber;
    private byte[] blockHash;

    public SnapshotManifestMessage(byte[] encoded) {
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

        RLPList params = (RLPList) paramsList.get(0);

        RLPList statesRLP = (RLPList) params.get(0);
        stateHashes = new ArrayList<>();
        for (RLPElement stateHash : statesRLP) {
            stateHashes.add(stateHash.getRLPData());
        }

        RLPList blockHashesRLP = (RLPList) params.get(1);
        blockHashes = new ArrayList<>();
        for (RLPElement blockHash : blockHashesRLP) {
            blockHashes.add(blockHash.getRLPData());
        }

        stateRoot = params.get(2).getRLPData();
        blockNumber = ByteUtil.byteArrayToLong(params.get(3).getRLPData());
        blockHash = params.get(4).getRLPData();

        parsed = true;
    }

    public List<byte[]> getStateHashes() {
        return stateHashes;
    }

    public List<byte[]> getBlockHashes() {
        return blockHashes;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public ParMessageCodes getCommand() {
        return ParMessageCodes.SNAPSHOT_MANIFEST;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();

        return String.format("SNAPSHOT_MANIFEST [ blockNumber: %d hash:%s stateRoot: %s ]",
                blockNumber,
                blockHash == null ? "" : Hex.toHexString(blockHash),
                stateRoot == null ? "" : Hex.toHexString(stateRoot));
    }
}