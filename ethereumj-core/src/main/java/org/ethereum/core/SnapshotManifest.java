package org.ethereum.core;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

/**
 * Snapshot Manifest contains best manifest block info and chunk hashes
 */
public class SnapshotManifest {

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
    public String toString() {
        return String.format(
                "SnapshotManifest {Manifest block: #%d [%s], stateRoot=%s, stateHashes(%s), blockHashes(%s)}",
                blockNumber,
                Hex.toHexString(blockHash),
                Hex.toHexString(stateRoot),
                stateHashes == null ? 0 : stateHashes.size(),
                blockHashes == null ? 0 : blockHashes.size()
        );
    }
}
