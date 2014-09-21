package org.ethereum.net.message;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.WorldManager;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 13/04/14 20:19
 */
public class StaticMessages {

    public final static PingMessage     PING_MESSAGE             = new PingMessage();
    public final static PongMessage     PONG_MESSAGE             = new PongMessage();
    public final static GetPeersMessage GET_PEERS_MESSAGE        = new GetPeersMessage();
    public final static GetTransactionsMessage GET_TRANSACTIONS_MESSAGE = new GetTransactionsMessage();

    public static final byte[] PING_PACKET		= Hex.decode("2240089100000002C102");
    public static final byte[] PONG_PACKET		= Hex.decode("2240089100000002C103");
    public static final byte[] GET_PEERS_PACKET	= Hex.decode("2240089100000002C110");
    public static final byte[] GET_TRANSACTIONS = Hex.decode("2240089100000002C116");

    public static final byte[] DISCONNECT_08 = Hex.decode("2240089100000003C20108");

    public static final byte[] SYNC_TOKEN = Hex.decode("22400891");
    public static final byte[] GENESIS_HASH = Genesis.getInstance().getHash();

    static {
        HELLO_MESSAGE = generateHelloMessage();
    }
    public static HelloMessage HELLO_MESSAGE;
    public static HelloMessage generateHelloMessage() {
        byte[] peerIdBytes = HashUtil.randomPeerId();

        String version = SystemProperties.CONFIG.projectVersion();
        String system = System.getProperty("os.name");
		if (system.contains(" "))
			system = system.substring(0, system.indexOf(" "));
        if (System.getProperty("java.vm.vendor").contains("Android")) 
        	system = "Android";

        String phrase = SystemProperties.CONFIG.helloPhrase();

        String helloAnnouncement = String.format("Ethereum(J)/v%s/%s/%s/Java", version, phrase, system);

        Block lastBlock = WorldManager.getInstance().getBlockchain().getLastBlock();
        byte[] totalDifficulty = lastBlock.getDifficulty();
        byte[] bestHash = lastBlock.getHash();
        
        return new HelloMessage((byte) 0x21, (byte) 0x00,
                helloAnnouncement, Byte.parseByte("00000111", 2),
                (short) 30303, peerIdBytes, totalDifficulty, bestHash, GENESIS_HASH);
    }
}
