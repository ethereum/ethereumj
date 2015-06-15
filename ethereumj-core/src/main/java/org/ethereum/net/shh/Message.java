package org.ethereum.net.shh;

import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.spongycastle.util.BigIntegers;

import org.spongycastle.math.ec.ECPoint;

import java.security.SignatureException;
import java.util.Random;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.net.shh.ShhMessageCodes.MESSAGE;
import static org.ethereum.util.ByteUtil.merge;

/**
 * Created by kest on 6/12/15.
 */
public class Message extends ShhMessage {

    private byte flags;
    private byte[] signature;
    private byte[] payload;

    private long sent;
    private long ttl;

    private byte[] envelopeHash;

    public static final byte SIGNATURE_FLAG = 127;
    public static final int SIGNATURE_LENGTH = 65;

//    public Message(byte[] encoded) {
//        super(encoded);
//    }

    public Message(byte[] payload) {
        super(null);
        Random r = new Random();
        byte[] randByte = new byte[1];
        r.nextBytes(randByte);
        flags = randByte[0];
        if (flags < 0) {
            flags = (byte)(flags & 0xF);
        }
        flags &= ~SIGNATURE_FLAG;

        this.sent = System.currentTimeMillis();
        this.payload = payload;
    }

    public Message(byte flags, long sent, long ttl, byte[] envelopeHash) {
        this.flags = flags;
        this.sent = sent;
        this.ttl = ttl;
        this.envelopeHash = envelopeHash;
    }

    public Envelope wrap(long pow, Options options) {
        //check ttl is not null
        if (options.getPrivateKey() != null) {
            sign(options.getPrivateKey());
        }

        if (options.getToPublicKey() != null) {
            encrypt(options.getToPublicKey());
        }

        Envelope e = new Envelope(options.getTtl(), options.getTopics(), this);
        return e;
    }

    public byte getFlags() {
        return flags;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getBytes() {
        if (signature != null) {
            return merge(new byte[]{flags}, signature, payload);
        } else {
            return merge(new byte[]{flags}, payload);
        }
    }

    private void encrypt(byte[] toPublicKey) {
        try {
            ECKey key = ECKey.fromPublicOnly(toPublicKey);
            ECPoint pubKeyPoint = key.getPubKeyPoint();
            payload = ECIESCoder.encrypt(pubKeyPoint, payload);
        } catch (Exception e) {

        }
    }

    public boolean decrypt(ECKey privateKey) {
        try {
            payload = ECIESCoder.decrypt(privateKey.getPrivKey(), payload);
            return true;
        } catch (Exception e) {
            System.out.println("The message payload isn't encrypted or something is wrong");
        } catch (Throwable e) {

        }
        return false;
    }

    private void sign(ECKey privateKey) {
        flags |= SIGNATURE_FLAG;
        byte[] forSig = hash();

        ECKey.ECDSASignature signature = privateKey.sign(forSig);

        this.signature =
                merge(BigIntegers.asUnsignedByteArray(signature.r),
                        BigIntegers.asUnsignedByteArray(signature.s), new byte[]{signature.v});
    }

    public ECKey recover() {
        if (signature == null) {
            return null;
        }

        byte[] r = new byte[32];
        byte[] s = new byte[32];
        byte v = signature[64];

        if (v == 1) v = 28;
        if (v == 0) v = 27;

        System.arraycopy(signature, 0, r, 0, 32);
        System.arraycopy(signature, 32, s, 0, 32);

        ECKey.ECDSASignature signature = ECKey.ECDSASignature.fromComponents(r, s, v);
        byte[] msgHash = hash();

        ECKey outKey = null;
        try {
            outKey = ECKey.signatureToKey(msgHash, signature.toBase64());
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return outKey;
    }

    private byte[] hash() {
        return sha3(merge(new byte[]{flags}, payload));
    }

    @Override
    public ShhMessageCodes getCommand() {
        return MESSAGE;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public String toString() {
        return this.toString();
    }

}
