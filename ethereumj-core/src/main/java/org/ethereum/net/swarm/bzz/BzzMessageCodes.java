package org.ethereum.net.swarm.bzz;

import java.util.HashMap;
import java.util.Map;

public enum BzzMessageCodes {


    /**
     * [0x00, [PROTOCOL_VERSION, NETWORK_ID, TD, BEST_HASH, GENESIS_HASH] <br>
     * Inform a peer of it's current ethereum state. This message should be
     * send after the initial handshake and prior to any ethereum related messages.
     */
    STATUS(0x01),

    /**
     * [+0x01] Request the peer to send all transactions
     * currently in the queue.
     */
    STORE_REQUEST(0x02),


    /**
     * [+0x03, [hash : B_32, maxBlocks: P]: <br>
     * Requests a BlockHashes message of at most maxBlocks entries,
     * of block hashes from the blockchain, starting at the parent of block hash.
     * Does not require the peer to give maxBlocks hashes -
     * they could give somewhat fewer.
     */
    RETRIEVE_REQUEST(0x03),

    /**
     * [+0x04, [hash_0: B_32, hash_1: B_32, ....]: <br>Gives a series of hashes
     * of blocks (each the child of the next). This implies that the blocks
     * are ordered from youngest to oldest.
     */
    PEERS(0x04);

    static byte OFFSET = 0;
    private int cmd;

    private static final Map<Integer, BzzMessageCodes> intToTypeMap = new HashMap<>();

    static {
        for (BzzMessageCodes type : BzzMessageCodes.values()) {
            intToTypeMap.put(type.cmd, type);
        }
    }

    private BzzMessageCodes(int cmd) {
        this.cmd = cmd;
    }

    public static BzzMessageCodes fromByte(byte i) {
        return intToTypeMap.get(i - OFFSET);
    }

    public static boolean inRange(byte code) {
        return code >= STATUS.asByte() && code <= PEERS.asByte();
    }

    public static void setOffset(byte offset) {
        BzzMessageCodes.OFFSET = offset;
    }

    public byte asByte() {
        return (byte) (cmd + OFFSET);
    }
}
