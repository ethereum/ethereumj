package org.ethereum.net.message;

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

    public static final byte[] GENESSIS_HASH = Hex.decode("69a7356a245f9dc5b865475ada5ee4e89b18f93c06503a9db3b3630e88e9fb4e");
    public static final byte[] MAGIC_PACKET = Hex.decode("22400891");

    static {
        String peerId  = "CE 73 F1 F1 F1 F1 6C 1B 3F DA 7B 18 EF 7B A3 CE " +
                         "17 B6 F1 F1 F1 F1 41 D3 C6 C6 54 B7 AE 88 B2 39 " +
                         "40 7F F1 F1 F1 F1 19 02 5D 78 57 27 ED 01 7B 6A " +
                         "DD 21 F1 F1 F1 F1 00 00 01 E3 21 DB C3 18 24 BA ";

        byte[] peerIdBytes = Utils.hexStringToByteArr(peerId);

        HELLO_MESSAGE = new HelloMessage((byte)0x0F, (byte)0x00, "EthereumJ [v0.0.1] pure java [by Roman Mandeleil]",
                (byte)0b00000111, (short)30303, peerIdBytes);

/*
        HELLO_MESSAGE = new HelloMessage((byte)0x0B, (byte)0x00, "EthereumJ [v0.0.1] pure java [by Roman Mandeleil]",
                (byte)0b00000111, (short)30303, peerIdBytes);
*/
    }

    public static final HelloMessage HELLO_MESSAGE;
}
