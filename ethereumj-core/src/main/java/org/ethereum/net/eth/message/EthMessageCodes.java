package org.ethereum.net.eth.message;

import org.ethereum.net.eth.EthVersion;

import java.util.HashMap;
import java.util.Map;

import static org.ethereum.net.eth.EthVersion.*;

/**
 * A list of commands for the Ethereum network protocol.
 * <br>
 * The codes for these commands are the first byte in every packet.
 *
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Wire-Protocol">
 * https://github.com/ethereum/wiki/wiki/Ethereum-Wire-Protocol</a>
 */
public enum EthMessageCodes {

    /* Ethereum protocol */

    /**
     * {@code [0x00, [PROTOCOL_VERSION, NETWORK_ID, TD, BEST_HASH, GENESIS_HASH] } <br>
     *
     * Inform a peer of it's current ethereum state. This message should be
     * send after the initial handshake and prior to any ethereum related messages.
     */
    STATUS(0x00),

    /**
     * PV 61 and lower <br>
     * {@code [+0x01, [hash_0: B_32, hash_1: B_32, ...] } <br>
     *
     * PV 62 and upper <br>
     * {@code [+0x01: P, [hash_0: B_32, number_0: P], [hash_1: B_32, number_1: P], ...] } <br>
     *
     * Specify one or more new blocks which have appeared on the network.
     * To be maximally helpful, nodes should inform peers of all blocks that they may not be aware of.
     * Including hashes that the sending peer could reasonably be considered to know
     * (due to the fact they were previously informed of because
     * that node has itself advertised knowledge of the hashes through NewBlockHashes)
     * is considered Bad Form, and may reduce the reputation of the sending node.
     * Including hashes that the sending node later refuses to honour with a proceeding
     * GetBlocks message is considered Bad Form, and may reduce the reputation of the sending node.
     *
     */
    NEW_BLOCK_HASHES(0x01),

    /**
     * {@code [+0x02, [nonce, receiving_address, value, ...], ...] } <br>
     *
     * Specify (a) transaction(s) that the peer should make sure is included
     * on its transaction queue. The items in the list (following the first item 0x12)
     * are transactions in the format described in the main Ethereum specification.
     */
    TRANSACTIONS(0x02),

    /**
     * {@code [+0x03: P, block: { P , B_32 }, maxHeaders: P, skip: P, reverse: P in { 0 , 1 } ] } <br>
     *
     * Replaces GetBlockHashes since PV 62. <br>
     *
     * Require peer to return a BlockHeaders message.
     * Reply must contain a number of block headers,
     * of rising number when reverse is 0, falling when 1, skip blocks apart,
     * beginning at block block (denoted by either number or hash) in the canonical chain,
     * and with at most maxHeaders items.
     */
    GET_BLOCK_HEADERS(0x03),

    /**
     * {@code [+0x04, blockHeader_0, blockHeader_1, ...] } <br>
     *
     * Replaces BLOCK_HASHES since PV 62. <br>
     *
     * Reply to GetBlockHeaders.
     * The items in the list (following the message ID) are
     * block headers in the format described in the main Ethereum specification,
     * previously asked for in a GetBlockHeaders message.
     * This may validly contain no block headers
     * if no block headers were able to be returned for the GetBlockHeaders query.
     */
    BLOCK_HEADERS(0x04),

    /**
     * {@code [+0x05, hash_0: B_32, hash_1: B_32, ...] } <br>
     *
     * Replaces GetBlocks since PV 62. <br>
     *
     * Require peer to return a BlockBodies message.
     * Specify the set of blocks that we're interested in with the hashes.
     */
    GET_BLOCK_BODIES(0x05),

    /**
     * {@code [+0x06, [transactions_0, uncles_0] , ...] } <br>
     *
     * Replaces Blocks since PV 62. <br>
     *
     * Reply to GetBlockBodies.
     * The items in the list (following the message ID) are some of the blocks, minus the header,
     * in the format described in the main Ethereum specification, previously asked for in a GetBlockBodies message.
     * This may validly contain no block headers
     * if no block headers were able to be returned for the GetBlockHeaders query.
     */
    BLOCK_BODIES(0x06),

    /**
     * {@code [+0x07 [blockHeader, transactionList, uncleList], totalDifficulty] } <br>
     *
     * Specify a single block that the peer should know about. The composite item
     * in the list (following the message ID) is a block in the format described
     * in the main Ethereum specification.
     */
    NEW_BLOCK(0x07);

    private int cmd;

    private static final Map<EthVersion, Map<Integer, EthMessageCodes>> intToTypeMap = new HashMap<>();
    private static final Map<EthVersion, EthMessageCodes[]> versionToValuesMap = new HashMap<>();

    static {

        versionToValuesMap.put(V62, new EthMessageCodes[]{
                STATUS,
                NEW_BLOCK_HASHES,
                TRANSACTIONS,
                GET_BLOCK_HEADERS,
                BLOCK_HEADERS,
                GET_BLOCK_BODIES,
                BLOCK_BODIES,
                NEW_BLOCK
        });

        for (EthVersion v : EthVersion.values()) {
            Map<Integer, EthMessageCodes> map = new HashMap<>();
            intToTypeMap.put(v, map);
            for (EthMessageCodes code : values(v)) {
                map.put(code.cmd, code);
            }
        }
    }

    private EthMessageCodes(int cmd) {
        this.cmd = cmd;
    }

    public static EthMessageCodes[] values(EthVersion v) {
        return versionToValuesMap.get(v);
    }

    public static EthMessageCodes fromByte(byte i, EthVersion v) {
        Map<Integer, EthMessageCodes> map = intToTypeMap.get(v);
        return map.get((int) i);
    }

    public static boolean inRange(byte code, EthVersion v) {
        EthMessageCodes[] codes = values(v);
        return code >= codes[0].asByte() && code <= codes[codes.length - 1].asByte();
    }

    public byte asByte() {
        return (byte) (cmd);
    }
}
