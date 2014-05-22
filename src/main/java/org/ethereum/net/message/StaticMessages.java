package org.ethereum.net.message;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 13/04/14 20:19
 */
public class StaticMessages {

    public static final byte[] PING             = Hex.decode("2240089100000002C102");
    public static final byte[] PONG             = Hex.decode("2240089100000002C103");
    public static final byte[] GET_PEERS        = Hex.decode("2240089100000002C110");
    public static final byte[] GET_TRANSACTIONS = Hex.decode("2240089100000002C116");

    public static final byte[] DISCONNECT_08 = Hex.decode("2240089100000003C20108");
    public static final byte[] GENESIS_HASH = Hex.decode("c305511e7cb9b33767e50f5e94ecd7b1c51359a04f45183860ec6808d80b0d3f");
    public static final byte[] MAGIC_PACKET = Hex.decode("22400891");

    static {

        byte[] peerIdBytes = HashUtil.randomPeerId();

        HELLO_MESSAGE = new HelloMessage((byte)0x11, (byte)0x00, "EthereumJ [v0.5.1] by RomanJ  ",
                (byte)0b00000111, (short)30303, peerIdBytes);
    }

    public static final HelloMessage HELLO_MESSAGE;
}
