package org.ethereum.api.type;

public class BlockId {

    public static final BlockId PENDING = new BlockId();
    public static final BlockId LATEST = new BlockId();

    public static BlockId fromHash(Hash256 hash) {
        return null;
    }

    public static BlockId fromNumber(long number) {
        return null;
    }
}

