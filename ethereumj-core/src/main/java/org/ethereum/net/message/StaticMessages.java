package org.ethereum.net.message;

import org.ethereum.util.Utils;

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


    public static final byte[] GET_CHAIN = {

            (byte) 0x22,  (byte) 0x40,  (byte) 0x08,  (byte) 0x91,  (byte) 0x00,  (byte) 0x00,  (byte) 0x00,  (byte) 0x27,
            (byte) 0xF8,  (byte) 0x25,  (byte) 0x14,  (byte) 0xA0,  (byte) 0xAB,  (byte) 0x6B,  (byte) 0x9A,  (byte) 0x56,  (byte) 0x13,
            (byte) 0x97,  (byte) 0x0F,  (byte) 0xAA,  (byte) 0x77,  (byte) 0x1B,  (byte) 0x12,  (byte) 0xD4,  (byte) 0x49,
            (byte) 0xB2,  (byte) 0xE9,  (byte) 0xBB,  (byte) 0x92,  (byte) 0x5A,  (byte) 0xB7,  (byte) 0xA3,  (byte) 0x69,
            (byte) 0xF0,  (byte) 0xA4,  (byte) 0xB8,  (byte) 0x6B,  (byte) 0x28,  (byte) 0x6E,  (byte) 0x9D,  (byte) 0x54,
            (byte) 0x00,  (byte) 0x99,  (byte) 0xCF,  (byte) 0x82,  (byte) 0x01,  (byte) 0x00,
    };

    static {
        String peerId  = "CE 73 F1 F1 F1 F1 6C 1B 3F DA 7B 18 EF 7B A3 CE " +
                         "17 B6 F1 F1 F1 F1 41 D3 C6 C6 54 B7 AE 88 B2 39 " +
                         "40 7F F1 F1 F1 F1 19 02 5D 78 57 27 ED 01 7B 6A " +
                         "DD 21 F1 F1 F1 F1 00 00 01 E3 21 DB C3 18 24 BA ";

        byte[] peerIdBytes = Utils.hexStringToByteArr(peerId);


        HELLO_MESSAGE = new HelloMessage((byte)0x0C, (byte)0x00, "EthereumJ [v0.0.1] pure java [by Roman Mandeleil]",
                (byte)0b00000111, (short)30303, peerIdBytes);

/*
        HELLO_MESSAGE = new HelloMessage((byte)0x0B, (byte)0x00, "EthereumJ [v0.0.1] pure java [by Roman Mandeleil]",
                (byte)0b00000111, (short)30303, peerIdBytes);
*/

    }


    public static final HelloMessage HELLO_MESSAGE;
}
