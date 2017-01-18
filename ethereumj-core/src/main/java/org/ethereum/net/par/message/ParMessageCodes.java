package org.ethereum.net.par.message;

import org.ethereum.net.par.ParVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * A list of commands for the Parity1 network protocol.
 * <br>
 * The codes for these commands are the first byte in every packet.
 *
 * PAR1: @see <a href="https://github.com/ethcore/parity/wiki/Warp-Sync">https://github.com/ethcore/parity/wiki/Warp-Sync</a>
 * <br>
 * <a href="https://github.com/ethcore/parity/wiki/Warp-Sync-Snapshot-Format">
 *     https://github.com/ethcore/parity/wiki/Warp-Sync-Snapshot-Format</a>
 */
public enum ParMessageCodes {

    /* Parity sub-eth protocol */

    /**
     * {@code [0x00, [PROTOCOL_VERSION, NETWORK_ID, TD, BEST_HASH, GENESIS_HASH, SNAPSHOT_HASH, SNAPSHOT_NUMBER] } <br>
     *
     * Inform a peer of it's current ethereum state. This message should be
     * send after the initial handshake and prior to any ethereum related messages.
     *
     * Includes snapshot_hash (B_32) and snapshot_number (P) which signify the block hash and number
     * respectively of the peer's local snapshot.
     *
     */
    STATUS(0x00),

    /**
     * {@code [+0x11] } <br>
     *
     * Request a snapshot manifest in RLP form from a peer.
     */
    GET_SNAPSHOT_MANIFEST(0x11),

    /**
     * {@code [+0x12, [manifest] } <br>
     *
     * Respond to a GetSnapshotManifest message with either an empty RLP list or
     * a 1-item RLP list containing a snapshot manifest.
     */
    SNAPSHOT_MANIFEST(0x12);

    // TODO: Add chunk-related messages

    private int cmd;

    private static final Map<ParVersion, Map<Integer, ParMessageCodes>> intToTypeMap = new HashMap<>();
    private static final Map<ParVersion, ParMessageCodes[]> versionToValuesMap = new HashMap<>();

    static {
        versionToValuesMap.put(ParVersion.PAR1, new ParMessageCodes[]{
                STATUS,
                GET_SNAPSHOT_MANIFEST,
                SNAPSHOT_MANIFEST
        });

        for (ParVersion v : ParVersion.values()) {
            Map<Integer, ParMessageCodes> map = new HashMap<>();
            intToTypeMap.put(v, map);
            for (ParMessageCodes code : values(v)) {
                map.put(code.cmd, code);
            }
        }
    }

    private ParMessageCodes(int cmd) {
        this.cmd = cmd;
    }

    public static ParMessageCodes[] values(ParVersion v) {
        return versionToValuesMap.get(v);
    }

    public static ParMessageCodes fromByte(byte i, ParVersion v) {
        Map<Integer, ParMessageCodes> map = intToTypeMap.get(v);
        return map.get((int) i);
    }

    public static boolean inRange(byte code, ParVersion v) {
        ParMessageCodes[] codes = values(v);
        return code >= codes[0].asByte() && code <= codes[codes.length - 1].asByte();
    }

    public byte asByte() {
        return (byte) (cmd);
    }
}
