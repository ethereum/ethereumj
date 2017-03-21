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

import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPDump;
import org.ethereum.util.RLPTest;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Anton Nashatyrev on 25.09.2015.
 */
public class EnvelopeTest {

    @Test
    public void testBroadcast1() {
        WhisperMessage msg1 = new WhisperMessage()
                .setTopics(Topic.createTopics("Topic1", "Topic2"))
                .setPayload("Hello");
        WhisperMessage msg2 = new WhisperMessage()
                .setTopics(Topic.createTopics("Topic1", "Topic3"))
                .setPayload("Hello again");
        ShhEnvelopeMessage envelope = new ShhEnvelopeMessage(msg1, msg2);

        byte[] bytes = envelope.getEncoded();

        ShhEnvelopeMessage inbound = new ShhEnvelopeMessage(bytes);
        Assert.assertEquals(2, inbound.getMessages().size());
        WhisperMessage inMsg1 = inbound.getMessages().get(0);
        boolean b = inMsg1.decrypt(Collections.EMPTY_LIST,
                Arrays.asList(Topic.createTopics("Topic2", "Topic3")));
        Assert.assertTrue(b);
        Assert.assertEquals("Hello", new String(inMsg1.getPayload()));

        WhisperMessage inMsg2 = inbound.getMessages().get(1);
        b = inMsg2.decrypt(Collections.EMPTY_LIST,
                Arrays.asList(Topic.createTopics("Topic1", "Topic3")));
        Assert.assertTrue(b);
        Assert.assertEquals("Hello again", new String(inMsg2.getPayload()));
    }

    @Test
    public void testPow1() {
        ECKey from = new ECKey();
        ECKey to = new ECKey();
        System.out.println("From: " + Hex.toHexString(from.getPrivKeyBytes()));
        System.out.println("To: " + Hex.toHexString(to.getPrivKeyBytes()));
        WhisperMessage msg1 = new WhisperMessage()
                .setTopics(Topic.createTopics("Topic1", "Topic2"))
                .setPayload("Hello")
                .setFrom(from)
                .setTo(WhisperImpl.toIdentity(to))
                .setWorkToProve(1000);
        WhisperMessage msg2 = new WhisperMessage()
                .setTopics(Topic.createTopics("Topic1", "Topic3"))
                .setPayload("Hello again")
                .setWorkToProve(500);
        ShhEnvelopeMessage envelope = new ShhEnvelopeMessage(msg1, msg2);

        byte[] bytes = envelope.getEncoded();

//        System.out.println(RLPTest.dump(RLP.decode2(bytes), 0));

        ShhEnvelopeMessage inbound = new ShhEnvelopeMessage(bytes);
        Assert.assertEquals(2, inbound.getMessages().size());
        WhisperMessage inMsg1 = inbound.getMessages().get(0);
        boolean b = inMsg1.decrypt(Collections.singleton(to),
                Arrays.asList(Topic.createTopics("Topic2", "Topic3")));
        Assert.assertTrue(b);
        Assert.assertEquals("Hello", new String(inMsg1.getPayload()));
        Assert.assertEquals(msg1.getTo(), inMsg1.getTo());
        Assert.assertEquals(msg1.getFrom(), inMsg1.getFrom());
//        System.out.println(msg1.nonce + ": " + inMsg1.nonce + ", " + inMsg1.getPow());
        Assert.assertTrue(inMsg1.getPow() > 10);

        WhisperMessage inMsg2 = inbound.getMessages().get(1);
        b = inMsg2.decrypt(Collections.EMPTY_LIST,
                Arrays.asList(Topic.createTopics("Topic2", "Topic3")));
        Assert.assertTrue(b);
        Assert.assertEquals("Hello again", new String(inMsg2.getPayload()));
//        System.out.println(msg2.nonce + ": " + inMsg2.getPow());
        Assert.assertTrue(inMsg2.getPow() > 8);
    }

    @Test
    public void testCpp1() throws Exception {
        byte[] cipherText1 = Hex.decode("0469e324b8ab4a8e2bf0440548498226c9864d1210248ebf76c3396dd1748f0b04d347728b683993e4061998390c2cc8d6d09611da6df9769ebec888295f9be99e86ddad866f994a494361a5658d2b48d1140d73f71a382a4dc7ee2b0b5487091b0c25a3f0e6");
        ECKey priv = ECKey.fromPrivate(Hex.decode("d0b043b4c5d657670778242d82d68a29d25d7d711127d17b8e299f156dad361a"));
        ECKey pub = ECKey.fromPublicOnly(Hex.decode("04bd27a63c91fe3233c5777e6d3d7b39204d398c8f92655947eb5a373d46e1688f022a1632d264725cbc7dc43ee1cfebde42fa0a86d08b55d2acfbb5e9b3b48dc5"));
        byte[] plain1 = ECIESCoder.decryptSimple(priv.getPrivKey(), cipherText1);
        byte[] cipherText2 = ECIESCoder.encryptSimple(pub.getPubKeyPoint(), plain1);

//        System.out.println("Cipher1: " + Hex.toHexString(cipherText1));
//        System.out.println("Cipher2: " + Hex.toHexString(cipherText2));

        byte[] plain2 = ECIESCoder.decryptSimple(priv.getPrivKey(), cipherText2);

        Assert.assertArrayEquals(plain1, plain2);
    }
}
