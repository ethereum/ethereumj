/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.shh;

import org.junit.Assert;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.junit.Test;

import java.math.BigInteger;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author by Konstantin Shabalin
 */
public class ShhTest {

/*
    private byte[] payload = "Hello whisper!".getBytes();
    private ECKey privKey = ECKey.fromPrivate(BigInteger.TEN);
    private byte[] pubKey = privKey.getPubKey();
    private int ttl = 10000;
    private Topic[] topics = new Topic[]{
            new Topic("topic 1"),
            new Topic("topic 2"),
            new Topic("topic 3")};


    @Test */
/* Tests whether a message can be wrapped without any identity or encryption. *//*

    public void test1() {
        WhisperMessage sent = new WhisperMessage(payload);
        Options options = new Options(null, null, topics, ttl);
        ShhEnvelopeMessage e = sent.wrap(Options.DEFAULT_POW, options);

        RLPList rlpList = RLP.decode2(e.getEncoded());
        RLPList.recursivePrint(rlpList);
        System.out.println();

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() == null);

        WhisperMessage received = e.open(null);

        ECKey recovered = received.recover();
        assertTrue(recovered == null);
    }

    @Test */
/* Tests whether a message can be signed, and wrapped in plain-text. *//*

    public void test2() {
        WhisperMessage sent = new WhisperMessage(payload);
        Options options = new Options(privKey, null, topics, ttl);
        ShhEnvelopeMessage e = sent.wrap(Options.DEFAULT_POW, options);

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() != null);

        WhisperMessage received = e.open(null);
        ECKey recovered = received.recover();

        assertEquals(Hex.toHexString(pubKey), Hex.toHexString(recovered.getPubKey()));
    }

    @Test */
/* Tests whether a message can be encrypted and decrypted using an anonymous sender (i.e. no signature).*//*

    public void test3() {
        WhisperMessage sent = new WhisperMessage(payload);
        Options options = new Options(null, pubKey, topics, ttl);
        ShhEnvelopeMessage e = sent.wrap(Options.DEFAULT_POW, options);

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertNotEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() == null);

        WhisperMessage received = e.open(null);

        assertEquals(Hex.toHexString(sent.getBytes()), Hex.toHexString(received.getBytes()));

        ECKey recovered = received.recover();
        assertTrue(recovered == null);
    }

    @Test */
/* Tests whether a message can be properly signed and encrypted. *//*

    public void test4() {
        WhisperMessage sent = new WhisperMessage(payload);
        Options options = new Options(privKey, pubKey, topics, ttl);
        ShhEnvelopeMessage e = sent.wrap(Options.DEFAULT_POW, options);

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertNotEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() != null);

        WhisperMessage received = e.open(privKey);
        ECKey recovered = received.recover();
        sent.decrypt(privKey);

        assertEquals(Hex.toHexString(sent.getBytes()), Hex.toHexString(received.getBytes()));
        assertEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertEquals(Hex.toHexString(pubKey), Hex.toHexString(recovered.getPubKey()));
    }

    @Test
    public void test5() {
        ECKey fromKey = ECKey.fromPrivate(Hex.decode("ba43d10d069f0c41a8914849c1abeeac2a681b21ae9b60a6a2362c06e6eb1bc8"));
        ECKey toKey = ECKey.fromPrivate(Hex.decode("00000000069f0c41a8914849c1abeeac2a681b21ae9b60a6a2362c06e6eb1bc8"));
//        System.out.println("Sending from: " + Hex.toHexString(fromKey.getPubKey()) + "\n to: " + Hex.toHexString(toKey.getPubKey()));
        byte[] bytes = post(fromKey, Hex.toHexString(toKey.getPubKey()), new String[]{"myTopic"}, "Hello all!", 1, 1);

        ShhEnvelopeMessage envelope = new ShhEnvelopeMessage(bytes);
        WhisperMessage msg = envelope.open(toKey);
//        System.out.println("Received: " + envelope + "\n   " + msg);
        assertEquals("Hello all!", new String(msg.getPayload()));
    }

    public byte[] post(ECKey fromKey, String to, String[] topics, String payload, int ttl, int pow) {

        Topic[] topicList = Topic.createTopics(topics);

        WhisperMessage m = new WhisperMessage(payload.getBytes());

        Options options = new Options(
                fromKey,
                to == null ? null : Hex.decode(to),
                topicList,
                ttl
        );

        ShhEnvelopeMessage e = m.wrap(pow, options);
        return e.getEncoded();
    }
*/
}
