package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.util.TimeUtils.secondsToMillis;

/**
 * <p> Wraps {@link Block} </p>
 * Adds some additional data required by core during blocks processing
 *
 * @author Mikhail Kalinin
 * @since 24.07.2015
 */
public class BlockWrapper {

    private static final long SOLID_BLOCK_DURATION_THRESHOLD = secondsToMillis(60);

    private Block block;
    private long importFailedAt = 0;
    private long receivedAt = 0;
    private boolean newBlock;
    private byte[] nodeId;

    public BlockWrapper(Block block, byte[] nodeId) {
        this(block, false, nodeId);
    }

    public BlockWrapper(Block block, boolean newBlock, byte[] nodeId) {
        this.block = block;
        this.newBlock = newBlock;
        this.nodeId = nodeId;
    }

    public BlockWrapper(byte[] bytes) {
        parse(bytes);
    }

    public Block getBlock() {
        return block;
    }

    public boolean isNewBlock() {
        return newBlock;
    }

    public boolean isSolidBlock() {
        return !newBlock || timeSinceReceiving() > SOLID_BLOCK_DURATION_THRESHOLD;
    }

    public long getImportFailedAt() {
        return importFailedAt;
    }

    public void setImportFailedAt(long importFailedAt) {
        this.importFailedAt = importFailedAt;
    }

    public byte[] getHash() {
        return block.getHash();
    }

    public long getNumber() {
        return block.getNumber();
    }

    public byte[] getEncoded() {
        return block.getEncoded();
    }

    public String getShortHash() {
        return block.getShortHash();
    }

    public byte[] getParentHash() {
        return block.getParentHash();
    }

    public long getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(long receivedAt) {
        this.receivedAt = receivedAt;
    }

    public byte[] getNodeId() {
        return nodeId;
    }

    public void importFailed() {
        if (importFailedAt == 0) {
            importFailedAt = System.currentTimeMillis();
        }
    }

    public void resetImportFail() {
        importFailedAt = 0;
    }

    public long timeSinceFail() {
        if(importFailedAt == 0) {
            return 0;
        } else {
            return System.currentTimeMillis() - importFailedAt;
        }
    }

    public long timeSinceReceiving() {
        return System.currentTimeMillis() - receivedAt;
    }

    public byte[] getBytes() {
        byte[] blockBytes = block.getEncoded();
        byte[] importFailedBytes = RLP.encodeBigInteger(BigInteger.valueOf(importFailedAt));
        byte[] receivedAtBytes = RLP.encodeBigInteger(BigInteger.valueOf(receivedAt));
        byte[] newBlockBytes = RLP.encodeByte((byte) (newBlock ? 1 : 0));
        byte[] nodeIdBytes = RLP.encodeElement(nodeId);
        return RLP.encodeList(blockBytes, importFailedBytes,
                receivedAtBytes, newBlockBytes, nodeIdBytes);
    }

    private void parse(byte[] bytes) {
        List<RLPElement> params = RLP.decode2(bytes);
        List<RLPElement> wrapper = (RLPList) params.get(0);

        byte[] blockBytes = wrapper.get(0).getRLPData();
        byte[] importFailedBytes = wrapper.get(1).getRLPData();
        byte[] receivedAtBytes = wrapper.get(2).getRLPData();
        byte[] newBlockBytes = wrapper.get(3).getRLPData();

        this.block = new Block(blockBytes);
        this.importFailedAt = importFailedBytes == null ? 0 : new BigInteger(1, importFailedBytes).longValue();
        this.receivedAt = receivedAtBytes == null ? 0 : new BigInteger(1, receivedAtBytes).longValue();
        byte newBlock = newBlockBytes == null ? 0 : new BigInteger(1, newBlockBytes).byteValue();
        this.newBlock = newBlock == 1;
        this.nodeId = wrapper.get(4).getRLPData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockWrapper wrapper = (BlockWrapper) o;

        return block.isEqual(wrapper.block);
    }
}
