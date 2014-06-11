package org.ethereum.net.message;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 13/04/14 20:19
 */
public class StaticMessages {

    public static final byte[] PING             = Hex.decode("2240089100000002C102");
    public static final byte[] PONG             = Hex.decode("2240089100000002C103");
    public static final byte[] GET_PEERS        = Hex.decode("2240089100000002C110");
    public static final byte[] GET_TRANSACTIONS = Hex.decode("2240089100000002C116");

    public static final byte[] DISCONNECT_08 = Hex.decode("2240089100000003C20108");
    public static final byte[] GENESIS_HASH = Hex.decode("56fff6ab5ef6f1ef8dafb7b4571b89a9ae1ab870e54197c59ea10ba6f2c7eb60");
    public static final byte[] MAGIC_PACKET = Hex.decode("22400891");

    static {
        HELLO_MESSAGE = generateHelloMessage();
    }
    public static HelloMessage HELLO_MESSAGE;
    public static HelloMessage generateHelloMessage(){
        byte[] peerIdBytes = HashUtil.randomPeerId();

        return new HelloMessage((byte) 0x14, (byte) 0x00,
                "EthereumJ [v0.5.1]  by RomanJ", (byte) 0b00000111,
                (short) 30303, peerIdBytes);
    }
}
