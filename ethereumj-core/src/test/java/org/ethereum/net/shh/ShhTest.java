package org.ethereum.net.shh;

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

    private byte[] payload = "Hello whisper!".getBytes();
    private ECKey privKey = ECKey.fromPrivate(BigInteger.TEN);
    private byte[] pubKey = privKey.decompress().getPubKey();
    private int ttl = 10000;
    private Topic[] topics = new Topic[]{
            new Topic("topic 1"),
            new Topic("topic 2"),
            new Topic("topic 3")};


    @Test /* Tests whether a message can be wrapped without any identity or encryption. */
    public void test1() {
        Message sent = new Message(payload);
        Options options = new Options(null, null, topics, ttl);
        Envelope e = sent.wrap(Options.DEFAULT_POW, options);

        RLPList rlpList = RLP.decode2(e.getEncoded());
        RLPList.recursivePrint(rlpList);
        System.out.println();

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() == null);

        Message received = e.open(null);

        ECKey recovered = received.recover();
        assertTrue(recovered == null);
    }

    @Test /* Tests whether a message can be signed, and wrapped in plain-text. */
    public void test2() {
        Message sent = new Message(payload);
        Options options = new Options(privKey, null, topics, ttl);
        Envelope e = sent.wrap(Options.DEFAULT_POW, options);

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() != null);

        Message received = e.open(null);
        ECKey recovered = received.recover();

        assertEquals(Hex.toHexString(pubKey), Hex.toHexString(recovered.decompress().getPubKey()));
    }

    @Test /* Tests whether a message can be encrypted and decrypted using an anonymous sender (i.e. no signature).*/
    public void test3() {
        Message sent = new Message(payload);
        Options options = new Options(null, pubKey, topics, ttl);
        Envelope e = sent.wrap(Options.DEFAULT_POW, options);

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertNotEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() == null);

        Message received = e.open(null);

        assertEquals(Hex.toHexString(sent.getBytes()), Hex.toHexString(received.getBytes()));

        ECKey recovered = received.recover();
        assertTrue(recovered == null);
    }

    @Test /* Tests whether a message can be properly signed and encrypted. */
    public void test4() {
        Message sent = new Message(payload);
        Options options = new Options(privKey, pubKey, topics, ttl);
        Envelope e = sent.wrap(Options.DEFAULT_POW, options);

        assertEquals(Hex.toHexString(e.getData()), Hex.toHexString(sent.getBytes()));
        assertNotEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertTrue(sent.getSignature() != null);

        Message received = e.open(privKey);
        ECKey recovered = received.recover();
        sent.decrypt(privKey);

        assertEquals(Hex.toHexString(sent.getBytes()), Hex.toHexString(received.getBytes()));
        assertEquals(Hex.toHexString(sent.getPayload()), Hex.toHexString(payload));
        assertEquals(Hex.toHexString(pubKey), Hex.toHexString(recovered.decompress().getPubKey()));
    }


}
