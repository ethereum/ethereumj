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
                .setTo(to.getPubKey())
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
//        byte[] env = Hex.decode("f872f870845609a1ba64c0b8660480136e573eb81ac4a664f8f76e4887ba927f791a053ec5ff580b1037a8633320ca70f8ec0cdea59167acaa1debc07bc0a0b3a5b41bdf0cb4346c18ddbbd2cf222f54fed795dde94417d2e57f85a580d87238efc75394ca4a92cfe6eb9debcc3583c26fee8580");
        byte[] env = Hex.decode("0428a562b1bfdeba62eea54d847ef4745a55edb4d0a02577bc419948666d1181258b7cda5c8e319e819b231e49a92d16517b6677bd698516c5d555eb68053911fb36e397d5db354779a13030fcf79864f7112ef98942bc5b8c2f3015f9661223db05e9a6bdc3");
        ECKey k = ECKey.fromPrivate(Hex.decode("d3a4a240b107ab443d46187306d0b947ce3d6b6ed95aead8c4941afcebde43d2"));
//        byte[] d1 = k.decryptAES(env);
        byte[] decrypt = ECIESCoder.decryptFuck(k.getPrivKey(), env);
//        byte[] decrypt = ECIESCoder.decrypt(k.getPrivKey(), env);

        ShhEnvelopeMessage inbound = new ShhEnvelopeMessage(env);
        WhisperMessage message = inbound.getMessages().get(0);
        boolean ret = message.decrypt(Collections.singleton(ECKey.fromPrivate(Hex.decode("d3a4a240b107ab443d46187306d0b947ce3d6b6ed95aead8c4941afcebde43d2"))),
                Collections.EMPTY_LIST);
        System.out.println(ret);
    }
    @Test
    public void testCpp2() throws Exception {
        testCpp1();

        ECKey to = ECKey.fromPublicOnly(Hex.decode("04deadbeea2250b3efb9e6268451e74bdbdc5632a1a03a0f5b626f59150ff772ac287e122531b5e8d55ff10cb541bbc8abf5def6bcbfa31cf5923ca3c3d783d312"));
//        System.out.println("To: " + Hex.toHexString(to.getPrivKeyBytes()));
        WhisperMessage msg1 = new WhisperMessage()
                .setPayload("Hello")
                .setTo(to.getPubKey());
        ShhEnvelopeMessage envelope = new ShhEnvelopeMessage(msg1);

        byte[] bytes = envelope.getEncoded();

        System.out.println(RLPDump.dump(RLP.decode2(bytes), 0));
    }
}
