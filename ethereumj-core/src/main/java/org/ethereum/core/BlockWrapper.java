package org.ethereum.core;

import java.math.BigInteger;

/**
 * <p> Wraps {@link Block} </p>
 * Adds some additional data required by core during blocks processing
 *
 * @author Mikhail Kalinin
 * @since 24.07.2015
 */
public class BlockWrapper {

    private Block block;
    private long importFailedAt = 0;
    private boolean newBlock;

    public BlockWrapper(Block block) {
        this(block, false);
    }

    public BlockWrapper(Block block, boolean newBlock) {
        this.block = block;
        this.newBlock = newBlock;
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

    public long getImportFailedAt() {
        return importFailedAt;
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

    public void importFailed() {
        if (importFailedAt == 0) {
            importFailedAt = System.currentTimeMillis();
        }
    }

    public long timeSinceFail() {
        if(importFailedAt == 0) {
            return 0;
        } else {
            return System.currentTimeMillis() - importFailedAt;
        }
    }

    public byte[] getBytes() {
        byte[] blockBytes = block.getEncoded();
        byte[] importFailedBytes = BigInteger.valueOf(importFailedAt).toByteArray();
        byte[] bytes = new byte[blockBytes.length + importFailedBytes.length + 2];
        bytes[0] = (byte) (newBlock ? 1 : 0);
        bytes[1] = (byte) importFailedBytes.length;
        System.arraycopy(importFailedBytes, 0, bytes, 2, importFailedBytes.length);
        System.arraycopy(blockBytes, 0, bytes, importFailedBytes.length + 2, blockBytes.length);
        return bytes;
    }

    private void parse(byte[] bytes) {
        byte[] importFailedAtBytes = new byte[bytes[1]];
        System.arraycopy(bytes, 2, importFailedAtBytes, 0, importFailedAtBytes.length);

        byte[] blockBytes = new byte[bytes.length - importFailedAtBytes.length - 2];
        System.arraycopy(bytes, importFailedAtBytes.length + 2, blockBytes, 0, blockBytes.length);

        this.newBlock = bytes[0] == 1;
        this.importFailedAt = new BigInteger(importFailedAtBytes).longValue();
        this.block = new Block(blockBytes);
    }
}
