package org.ethereum.net.message;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 13/04/14 20:19
 */
public class StaticMessages {

    public static final byte[] PING = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91,
                                       (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02,
                                       (byte)0xC1, (byte)0x02 };

    public static final byte[] PONG = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91,
                                       (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02,
                                       (byte)0xC1, (byte)0x03 };

    public static final byte[] GET_PEERS = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91,
                                            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02,
                                            (byte)0xC1, (byte)0x10 };

    public static final byte[] GET_TRANSACTIONS = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91,
                                                   (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02,
                                                   (byte)0xC1, (byte)0x16 };


    public static final byte[] DISCONNECT_00 = Hex.decode("2240089100000003C20100");
    public static final byte[] GET_CHAIN = Hex.decode("2240089100000027F82514A069A7356A245F9DC5B865475ADA5EE4E89B18F93C06503A9DB3B3630E88E9FB4E820100");
    public static final byte[] GENESIS_HASH = Hex.decode("f5232afe32aba6b366f8aa86a6939437c5e13d1fd71a0f51e77735d3456eb1a6");
    public static final byte[] MAGIC_PACKET = Hex.decode("22400891");

    static {

        byte[] peerIdBytes = HashUtil.randomPeerId();

                                                        // Hey Nick I like it that way ;)
        HELLO_MESSAGE = new HelloMessage((byte)0x10, (byte)0x00, "EthereumJ [v0.5.1] by RomanJ ",
                (byte)0b00000111, (short)30303, peerIdBytes);
    }

    public static final HelloMessage HELLO_MESSAGE;
}
