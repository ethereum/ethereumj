package org.ethereum.net.eth.message;

import java.util.HashMap;
import java.util.Map;

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
     * [0x00, [PROTOCOL_VERSION, NETWORK_ID, TD, BEST_HASH, GENESIS_HASH, { PV61: BEST_NUMBER }] <br>
     * Inform a peer of it's current ethereum state. This message should be
     * send after the initial handshake and prior to any ethereum related messages.
     */
    STATUS(0x00),

    /**
     * [+0x01, [hash1: B_32, hash2: B_32, ...]: <br>
     * Specify one or more new blocks which have appeared on the network.
     * To be maximally helpful, nodes should inform peers of all blocks that they may not be aware of.
     * Including hashes that the sending peer could reasonably be considered to know
     * (due to the fact they were previously informed of because
     * that node has itself advertised knowledge of the hashes through NewBlockHashes)
     * is considered Bad Form, and may reduce the reputation of the sending node.
     * Including hashes that the sending node later refuses to honour with a proceeding
     * GetBlocks message is considered Bad Form, and may reduce the reputation of the sending node.
     */
    NEW_BLOCK_HASHES(0x01),

    /**
     * [+0x02, [nonce, receiving_address, value, ...], ...] <br>
     * Specify (a) transaction(s) that the peer should make sure is included
     * on its transaction queue. The items in the list (following the first item 0x12)
     * are transactions in the format described in the main Ethereum specification.
     */
    TRANSACTIONS(0x02),

    /**
     * [+0x03, [hash : B_32, maxBlocks: P]: <br>
     * Requests a BlockHashes message of at most maxBlocks entries,
     * of block hashes from the blockchain, starting at the parent of block hash.
     * Does not require the peer to give maxBlocks hashes -
     * they could give somewhat fewer.
     */
    GET_BLOCK_HASHES(0x03),

    /**
     * [+0x04, [hash_0: B_32, hash_1: B_32, ....]: <br>Gives a series of hashes
     * of blocks (each the child of the next). This implies that the blocks
     * are ordered from youngest to oldest.
     */
    BLOCK_HASHES(0x04),

    /**
     * [+0x05, [hash_0: B_32, hash_1: B_32, ....]: <br>Requests a Blocks message
     * detailing a number of blocks to be sent, each referred to by a hash. <br>
     * <b>Note:</b> Don't expect that the peer necessarily give you all these blocks
     * in a single message - you might have to re-request them.
     */
    GET_BLOCKS(0x05),

    /**
     * [+0x06, [block_header, transaction_list, uncle_list], ...] <br>
     * Specify (a) block(s) that the peer should know about.
     * The items in the list (following the first item, 0x13)
     * are blocks in the format described in the main Ethereum specification.
     */
    BLOCKS(0x06),

    /**
     * [+0x07 [blockHeader, transactionList, uncleList], totalDifficulty] <br>
     * Specify a single block that the peer should know about. The composite item
     * in the list (following the message ID) is a block in the format described
     * in the main Ethereum specification.
     */
    NEW_BLOCK(0x07),

    /**
     * [+0x08, [number: P, maxBlocks: P]: <br>
     * Requires peer to reply with a BlockHashes message.
     * Message should contain block with that of number number on the canonical chain.
     * Should also be followed by subsequent blocks, on the same chain,
     * detailing a number of the first block hash and a total of hashes to be sent.
     * Returned hash list must be ordered by block number in ascending order.
     */
    GET_BLOCK_HASHES_BY_NUMBER(0x08);


    static byte OFFSET = 0;
    private int cmd;

    private static final Map<Integer, EthMessageCodes> intToTypeMap = new HashMap<>();

    static {
        for (EthMessageCodes type : EthMessageCodes.values()) {
            intToTypeMap.put(type.cmd, type);
        }
    }

    private EthMessageCodes(int cmd) {
        this.cmd = cmd;
    }

    public static EthMessageCodes fromByte(byte i) {
        return intToTypeMap.get((int) i);
    }

    public static boolean inRange(byte code) {
        EthMessageCodes[] codes = values();
        return code >= codes[0].asByte() && code <= codes[codes.length - 1].asByte();
    }

    public static void setOffset(byte offset) {
        EthMessageCodes.OFFSET = offset;
    }

    public byte asByte() {
        return (byte) (cmd);
    }
}
