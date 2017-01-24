package org.ethereum.core;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Snapshot Manifest contains best manifest block info and chunk hashes
 */
public class SnapshotManifest {

    private byte[] encoded;

    private List<byte[]> stateHashes;
    private List<byte[]> blockHashes;
    private byte[] stateRoot;
    private Long blockNumber;
    private byte[] blockHash;

    public SnapshotManifest(List<byte[]> stateHashes, List<byte[]> blockHashes,
                            byte[] stateRoot, Long blockNumber, byte[] blockHash) {
        this.stateHashes = stateHashes;
        this.blockHashes = blockHashes;
        this.stateRoot = stateRoot;
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
    }

    // TODO: may be pass somethhin encoded from Message?
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    private synchronized void encode() {
        byte[][] encodedStateHashesArray = stateHashes
                .toArray(new byte[stateHashes.size()][]);
        byte[] stateHashesRlp = RLP.encodeList(encodedStateHashesArray);

        byte[][] encodedBlockHashesArray = blockHashes
                .toArray(new byte[blockHashes.size()][]);
        byte[] blockHashesRlp = RLP.encodeList(encodedBlockHashesArray);

        byte[] stateRootRlp = RLP.encodeElement(stateRoot);
        byte[] blockNumberRlp = ByteUtil.longToBytes(blockNumber);
        byte[] blockHashRlp = RLP.encodeElement(blockHash);

        encoded = RLP.encodeList(stateHashesRlp, blockHashesRlp, stateRootRlp, blockNumberRlp, blockHashRlp);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnapshotManifest that = (SnapshotManifest) o;
        return FastByteComparisons.equal(stateRoot, that.stateRoot) &&
                Objects.equals(blockNumber, that.blockNumber) &&
                FastByteComparisons.equal(blockHash, that.blockHash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(stateRoot);
    }

    @Override
    public String toString() {
        return String.format(
                "SnapshotManifest {Manifest block: #%d [%s], stateRoot=%s, stateHashes(%s), blockHashes(%s)}",
                blockNumber,
                blockHash == null ? "null" : Hex.toHexString(blockHash),
                stateRoot == null ? "null" : Hex.toHexString(stateRoot),
                stateHashes == null ? 0 : stateHashes.size(),
                blockHashes == null ? 0 : blockHashes.size()
        );
    }
}
