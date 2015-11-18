package org.ethereum.net.shh;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A list of commands for the Whisper network protocol.
 * <br>
 * The codes for these commands are the first byte in every packet.
 *
 * @see <a href="https://github.com/ethereum/wiki/wiki/Wire-Protocol">
 * https://github.com/ethereum/wiki/wiki/Wire-Protocol</a>
 */
public enum ShhMessageCodes {

    /* Whisper ProtocolHandler */

    /**
     * [+0x00]
     */
    STATUS(0x00),

    /**
     * [+0x01]
     */
    MESSAGE(0x01),

    /**
     * [+0x02]
     */
    FILTER(0x02);

    private final int cmd;

    private static final TreeMap<Integer, ShhMessageCodes> intToTypeMap = new TreeMap<>();

    static {
        for (ShhMessageCodes type : ShhMessageCodes.values()) {
            intToTypeMap.put(type.cmd, type);
        }
    }

    private ShhMessageCodes(int cmd) {
        this.cmd = cmd;
    }

    public static ShhMessageCodes fromByte(byte i) {
        return intToTypeMap.get((int) i);
    }

    public static boolean inRange(byte code) {
        return code >= STATUS.asByte() && code <= FILTER.asByte();
    }

    public static int max() { return intToTypeMap.lastKey(); }

    public byte asByte() {
        return (byte) (cmd);
    }
}
