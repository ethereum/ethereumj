package org.ethereum.net.message;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.Utils;
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


    public static final byte[] DISCONNECT_00 = {(byte)0x22, (byte)0x40, (byte)0x08, (byte)0x91,
                                                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03,
                                                (byte)0xC2, (byte)0x01, (byte)0x00};

    public static final byte[] DISCONNECT_01 = {(byte) 0x22, (byte) 0x40, (byte) 0x08, (byte) 0x91,
                                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
                                                (byte) 0xC2, (byte) 0x01, (byte) 0x01};

    public static final byte[] DISCONNECT_02 = {(byte) 0x22, (byte) 0x40, (byte) 0x08, (byte) 0x91,
                                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
                                                (byte) 0xC2, (byte) 0x01, (byte) 0x02};

    public static final byte[] DISCONNECT_03 = {(byte) 0x22, (byte) 0x40, (byte) 0x08, (byte) 0x91,
                                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
                                                (byte) 0xC2, (byte) 0x01, (byte) 0x03};

    public static final byte[] DISCONNECT_08 = {(byte) 0x22, (byte) 0x40, (byte) 0x08, (byte) 0x91,
                                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
                                                (byte) 0xC2, (byte) 0x01, (byte) 0x08};

    public static final byte[] GET_CHAIN = Hex.decode("2240089100000027F82514A069A7356A245F9DC5B865475ADA5EE4E89B18F93C06503A9DB3B3630E88E9FB4E820100");

    public static final byte[] GENESSIS_HASH = Hex.decode("f5232afe32aba6b366f8aa86a6939437c5e13d1fd71a0f51e77735d3456eb1a6");
    public static final byte[] MAGIC_PACKET = Hex.decode("22400891");

    static {

        byte[] peerIdBytes = HashUtil.randomPeerId();

        HELLO_MESSAGE = new HelloMessage((byte)0x10, (byte)0x00, "EthereumJ [v0.5.1] pure java by RomanJ",
                (byte)0b00000111, (short)30303, peerIdBytes);

/*
        HELLO_MESSAGE = new HelloMessage((byte)0x0B, (byte)0x00, "EthereumJ [v0.0.1] pure java [by Roman Mandeleil]",
                (byte)0b00000111, (short)30303, peerIdBytes);
*/
    }

    public static final HelloMessage HELLO_MESSAGE;
}
