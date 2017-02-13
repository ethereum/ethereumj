package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
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

    public SnapshotManifest(Long blockNumber, byte[] blockHash) {
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
    }

    public SnapshotManifest(byte[] encoded) {
        this.encoded = encoded;
        RLPList manifestContainerRlp = RLP.decode2(encoded);
        RLPList manifestRlp = (RLPList) manifestContainerRlp.get(0);

        stateHashes = new ArrayList<>();
        RLPList stateHashesRlp = (RLPList) manifestRlp.get(0);
        for (RLPElement stateHashRlp : stateHashesRlp) {
            stateHashes.add(stateHashRlp.getRLPData());
        }

        blockHashes = new ArrayList<>();
        RLPList blockHashesRlp = (RLPList) manifestRlp.get(1);
        for (RLPElement blockHashRlp : blockHashesRlp) {
            blockHashes.add(blockHashRlp.getRLPData());
        }

        stateRoot = manifestRlp.get(2).getRLPData();
        byte[] blockNumberBytes = manifestRlp.get(3).getRLPData();
        blockNumber = blockNumberBytes == null ? 0 : (new BigInteger(1, blockNumberBytes)).longValue();
        blockHash = manifestRlp.get(4).getRLPData();
    }

    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    private synchronized void encode() {
        byte[] stateHashesRlp = RLP.encodeList(stateHashes);
        byte[] blockHashesRlp = RLP.encodeList(blockHashes);
        byte[] stateRootRlp = RLP.encodeElement(stateRoot);
        byte[] blockNumberRlp = RLP.encodeBigInteger(BigInteger.valueOf(blockNumber));
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

    public void setStateHashes(List<byte[]> stateHashes) {
        this.stateHashes = stateHashes;
    }

    public void setBlockHashes(List<byte[]> blockHashes) {
        this.blockHashes = blockHashes;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public void setBlockHash(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    public byte[] getHash() {
        if (encoded == null) encode();
        return HashUtil.sha3(encoded);
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

    public String getShortDescr() {
        return String.format(
                "SnapshotManifest {Manifest block: #%d [%s]}",
                blockNumber,
                blockHash == null ? "null" : Hex.toHexString(blockHash)
        );
    }
}
