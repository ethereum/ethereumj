package org.ethereum.net.swarm.bzz;

import java.util.HashMap;
import java.util.Map;

public enum BzzMessageCodes {

    /**
     * Handshake BZZ message
     */
    STATUS(0x00),

    /**
     * Request to store a {@link org.ethereum.net.swarm.Chunk}
     */
    STORE_REQUEST(0x01),

    /**
     * Used for several purposes
     * - the main is to ask for a {@link org.ethereum.net.swarm.Chunk} with the specified hash
     * - ask to send back {#PEERS} message with the known nodes nearest to the specified hash
     * - initial request after handshake with zero hash. On this request the nearest known
     *   neighbours are sent back with the {#PEERS} message.
     */
    RETRIEVE_REQUEST(0x02),

    /**
     * The message is the immediate response on the {#RETRIEVE_REQUEST} with the nearest known nodes
     * of the requested hash.
     */
    PEERS(0x03);

    static byte OFFSET = 0;
    private int cmd;

    private static final Map<Integer, BzzMessageCodes> intToTypeMap = new HashMap<>();

    static {
        for (BzzMessageCodes type : BzzMessageCodes.values()) {
            intToTypeMap.put(type.cmd, type);
        }
    }

    BzzMessageCodes(int cmd) {
        this.cmd = cmd;
    }

    public static BzzMessageCodes fromByte(byte i) {
        return intToTypeMap.get((int) i);
    }

    public static boolean inRange(byte code) {
        return code >= STATUS.asByte() && code <= PEERS.asByte();
    }

    public static void setOffset(byte offset) {
        BzzMessageCodes.OFFSET = offset;
    }

    public byte asByte() {
        return (byte) (cmd);
    }
}
