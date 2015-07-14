package org.ethereum.net.eth;

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
     * [0x00, [PROTOCOL_VERSION, NETWORK_ID, TD, BEST_HASH, GENESIS_HASH] <br>
     * Inform a peer of it's current ethereum state. This message should be
     * send after the initial handshake and prior to any ethereum related messages.
     */
    STATUS(0x00),

    /**
     * [+0x01] Request the peer to send all transactions
     * currently in the queue.
     */
    GET_TRANSACTIONS(0x01),

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
    NEW_BLOCK(0x07);


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
        return code >= STATUS.asByte() && code <= NEW_BLOCK.asByte();
    }

    public static void setOffset(byte offset) {
        EthMessageCodes.OFFSET = offset;
    }

    public byte asByte() {
        return (byte) (cmd);
    }
}
