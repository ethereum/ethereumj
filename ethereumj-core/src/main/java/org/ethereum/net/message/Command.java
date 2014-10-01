package org.ethereum.net.message;

import java.util.HashMap;
import java.util.Map;

/**
 * A list of commands for the Ethereum network protocol.
 * <br/>
 * The codes for these commands are the first byte in every packet.
 * 
 * @see <a href="https://github.com/ethereum/wiki/wiki/Wire-Protocol">
 * https://github.com/ethereum/wiki/wiki/Wire-Protocol</a><br/>
 * <a href="https://github.com/ethereum/cpp-ethereum/wiki/%C3%90%CE%9EVP2P-Networking">
 * https://github.com/ethereum/cpp-ethereum/wiki/ÐΞVP2P-Networking</a><br/>
 * <a href="https://github.com/ethereum/cpp-ethereum/wiki/PoC-6-Network-Protocol">
 * https://github.com/ethereum/cpp-ethereum/wiki/PoC-6-Network-Protocol</a>
 */
public enum Command {

	/* P2P */
	
	/** [0x00, P2P_VERSION, CLIEND_ID, CAPS, LISTEN_PORT, CLIENT_ID] <br/>
	 * First packet sent over the connection, and sent once by both sides. 
	 * No other messages may be sent until a Hello is received. */
	HELLO(0x00),
	
	/** [0x01, REASON] <br/>Inform the peer that a disconnection is imminent; 
	 * if received, a peer should disconnect immediately. When sending, 
	 * well-behaved hosts give their peers a fighting chance (read: wait 2 seconds) 
	 * to disconnect to before disconnecting themselves. */
	DISCONNECT(0x01),
	
	/** [0x02] <br/>Requests an immediate reply of Pong from the peer. */
	PING(0x02),
	
	/** [0x03] <br/>Reply to peer's Ping packet. */
	PONG(0x03),
	
	/** [0x04] <br/>Request the peer to enumerate some known peers 
	 * for us to connect to. This should include the peer itself. */
	GET_PEERS(0x04),
	
	/** [0x05, [IP1, Port1, Id1], [IP2, Port2, Id2], ... ] <br/>
	 * Specifies a number of known peers. IP is a 4-byte array 'ABCD' 
	 * that should be interpreted as the IP address A.B.C.D. 
	 * Port is a 2-byte array that should be interpreted as a 
	 * 16-bit big-endian integer. Id is the 512-bit hash that acts 
	 * as the unique identifier of the node. */
	PEERS(0x05),

	/** [0x10, [PROTOCOL_VERSION, NETWORK_ID, TD, BEST_HASH, GENESIS_HASH] <br/>
	 * Inform a peer of it's current ethereum state. This message should be 
	 * send after the initial handshake and prior to any ethereum related messages. */
	STATUS(0x10),
	
	/* Ethereum */
	
	/** [0x11] 	 * Request the peer to send all transactions 
	 * currently in the queue. */
	GET_TRANSACTIONS(0x11),
	
	/** [0x12, [nonce, receiving_address, value, ... ], ... ] <br/>
	 * Specify (a) transaction(s) that the peer should make sure is included 
	 * on its transaction queue. The items in the list (following the first item 0x12) 
	 * are transactions in the format described in the main Ethereum specification. */
	TRANSACTIONS(0x12),

	/** [0x13, [ hash : B_32, maxBlocks: P ]: <br/>
	 * Requests a BlockHashes message of at most maxBlocks entries, 
	 * of block hashes from the blockchain, starting at the parent of block hash. 
	 * Does not require the peer to give maxBlocks hashes - 
	 * they could give somewhat fewer. */	
	GET_BLOCK_HASHES(0x13),
	
	/** [0x14, [ hash_0: B_32, hash_1: B_32, .... ]: <br/>Gives a series of hashes 
	 * of blocks (each the child of the next). This implies that the blocks 
	 * are ordered from youngest to oldest. */
	BLOCK_HASHES(0x14),
	
	/** [0x15, [ hash_0: B_32, hash_1: B_32, .... ]: <br/>Requests a Blocks message 
	 * detailing a number of blocks to be sent, each referred to by a hash. <br/>
	 * <b>Note:</b> Don't expect that the peer necessarily give you all these blocks 
	 * in a single message - you might have to re-request them. */
	GET_BLOCKS(0x15),
	
	/** [0x16, [block_header, transaction_list, uncle_list], ... ] <br/>
	 * Specify (a) block(s) that the peer should know about. 
	 * The items in the list (following the first item, 0x13) 
	 * are blocks in the format described in the main Ethereum specification. */
	BLOCKS(0x16),
	
	UNKNOWN(0xFF);

    private int cmd;
    
    private static final Map<Integer, Command> intToTypeMap = new HashMap<>();
    static {
        for (Command type : Command.values()) {
            intToTypeMap.put(type.cmd, type);
        }
    }
    
    private Command(int cmd) {
        this.cmd = cmd;
    }

    public static Command fromInt(int i) {
    	Command type = intToTypeMap.get(Integer.valueOf(i));
        if (type == null) 
            return Command.UNKNOWN;
        return type;
    }
    
    public byte asByte() {
    	return (byte) cmd;
    }
}
