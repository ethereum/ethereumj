package org.ethereum.net.shh;

import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.*;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.merge;

/**
 * Created by Anton Nashatyrev on 25.09.2015.
 */
public class WhisperMessage extends ShhMessage {
    private final static Logger logger = LoggerFactory.getLogger("net");

    public static final int SIGNATURE_FLAG = 128;
    public static final int SIGNATURE_LENGTH = 65;

    private Topic[] topics;
    private byte[] payload;
    private byte flags;
    private byte[] signature;
    private byte[] to;
    private ECKey from;

    private int expire;
    private int ttl;
    private int nonce;

    private boolean encrypted = false;

    public WhisperMessage() {
        setTtl(50);
        nonce = 50;
    }

    public WhisperMessage(byte[] encoded) {
        super(encoded);
        encrypted = true;
        parse();
    }

    public Topic[] getTopics() {
        return topics;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getExpire() {
        return expire;
    }

    public int getTtl() {
        return ttl;
    }

    public int getNonce() {
        return nonce;
    }

    public byte[] getFrom() {
        return from.getPubKey();
    }

    public byte[] getTo() {
        return to;
    }

    /***********   Decode routines   ************/

    private void parse() {
        if (!parsed) {
            RLPList paramsList = RLP.decode2(encoded);
            this.expire = ByteUtil.byteArrayToInt(paramsList.get(0).getRLPData());
            this.ttl = ByteUtil.byteArrayToInt(paramsList.get(1).getRLPData());

            List<Topic> topics = new ArrayList<>();
            RLPList topicsList = (RLPList) RLP.decode2(paramsList.get(2).getRLPData()).get(0);
            for (RLPElement e : topicsList) {
                topics.add(new Topic(e.getRLPData()));
            }
            this.topics = new Topic[topics.size()];
            topics.toArray(this.topics);

            byte[] data = paramsList.get(3).getRLPData();
            this.nonce = ByteUtil.byteArrayToInt(paramsList.get(4).getRLPData());
            flags = data[0];

            if ((flags & WhisperMessage.SIGNATURE_FLAG) != 0) {
                if (data.length < WhisperMessage.SIGNATURE_LENGTH) {
                    throw new Error("Unable to open the envelope. First bit set but len(data) < len(signature)");
                }
                signature = new byte[WhisperMessage.SIGNATURE_LENGTH];
                System.arraycopy(data, 1, signature, 0, WhisperMessage.SIGNATURE_LENGTH);
                payload = new byte[data.length - WhisperMessage.SIGNATURE_LENGTH - 1];
                System.arraycopy(data, WhisperMessage.SIGNATURE_LENGTH + 1, payload, 0, payload.length);
                from = recover().decompress();
            } else {
                payload = new byte[data.length - 1];
                System.arraycopy(data, 1, payload, 0, payload.length);
            }

            this.parsed = true;
        }
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean decrypt(ECKey privateKey) {
        try {
            payload = ECIESCoder.decrypt(privateKey.getPrivKey(), payload);
            to = privateKey.decompress().getPubKey();
            encrypted = false;
            return true;
        } catch (Exception e) {
            logger.info("Wrong identity or the message payload isn't encrypted");
        } catch (Throwable e) {

        }
        return false;
    }

    private ECKey.ECDSASignature decodeSignature() {
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

        return ECKey.ECDSASignature.fromComponents(r, s, v);
    }

    private ECKey recover() {
        ECKey.ECDSASignature signature = decodeSignature();
        if (signature == null) return null;

        byte[] msgHash = hash();

        ECKey outKey = null;
        try {
            outKey = ECKey.signatureToKey(msgHash, signature.toBase64());
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return outKey;
    }

    public byte[] hash() {
        return sha3(merge(new byte[]{flags}, payload));
    }

    /***********   Encode routines   ************/

    public WhisperMessage setTopics(Topic ... topics) {
        this.topics = topics;
        return this;
    }

    public WhisperMessage setPayload(String payload) {
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public WhisperMessage setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    /**
     * If set the message will be encrypted with the receiver public key
     * @param to public key
     */
    public WhisperMessage setTo(byte[] to) {
        this.to = to;
        return this;
    }

    /**
     * If set the message will be signed by the sender key
     * @param from sender key
     */
    public WhisperMessage setFrom(ECKey from) {
        this.from = from;
        return this;
    }

    public WhisperMessage setTtl(int ttl) {
        this.ttl = ttl;
        expire = (int) (Utils.toUnixTime(System.currentTimeMillis()) + ttl);
        return this;
    }

    public WhisperMessage setNonce(int nonce) {
        this.nonce = nonce;
        return this;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) {
            if (from != null) {
                sign();
            }
            if (to != null) {
                encrypt();
            }
            byte[] msgBytes = getBytes();

            byte[] expire = RLP.encode(this.expire);
            byte[] ttl = RLP.encode(this.ttl);

            List<byte[]> topics = new Vector<>();
            for (Topic t : this.topics) {
                topics.add(RLP.encodeElement(t.getBytes()));
            }
            byte[][] topicsArray = topics.toArray(new byte[topics.size()][]);
            byte[] encodedTopics = RLP.encodeList(topicsArray);

            byte[] data = RLP.encodeElement(msgBytes);

            // TODO: add POW

            byte[] nonce = RLP.encodeInt(this.nonce);

            this.encoded = RLP.encodeList(expire, ttl, encodedTopics, data, nonce);
        }
        return encoded;
    }

    public void seal(long pow) {
        byte[] d = new byte[64];
//        encode();
        byte[] rlp = this.encoded;
        int l = rlp.length < 32 ? rlp.length : 32;
        System.arraycopy(rlp, 0, d, 0, l);

        long then = System.currentTimeMillis() + pow;

        for (int bestBit = 0; System.currentTimeMillis() < then;) {
            for (int i = 0, nonce = 0; i < 1024; ++i, ++nonce) {
                byte[] nonceBytes = intToByteArray(nonce);
                System.arraycopy(nonceBytes, 0, d, 60, nonceBytes.length);
                int fbs = getFirstBitSet(sha3(d));
                if (fbs > bestBit) {
                    this.nonce = nonce;
                    bestBit = fbs;
                }
            }
        }
        this.encoded = null;
    }

    private int getFirstBitSet(byte[] bytes) {
        BitSet b = BitSet.valueOf(bytes);
        for (int i = 0; i < b.length(); i++) {
            if (b.get(i)) {
                return i;
            }
        }
        return 0;
    }

    private byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }


    public byte[] getBytes() {
        if (signature != null) {
            return merge(new byte[]{flags}, signature, payload);
        } else {
            return merge(new byte[]{flags}, payload);
        }
    }

    private void encrypt() {
        try {
            ECKey key = ECKey.fromPublicOnly(to);
            ECPoint pubKeyPoint = key.getPubKeyPoint();
            payload = ECIESCoder.encrypt(pubKeyPoint, payload);
        } catch (Exception e) {
            logger.error("Unexpected error while encrypting: ", e);
        }
    }

    private void sign() {
        flags |= SIGNATURE_FLAG;
        byte[] forSig = hash();

        ECKey.ECDSASignature signature = from.sign(forSig);

        this.signature =
                merge(BigIntegers.asUnsignedByteArray(32, signature.r),
                        BigIntegers.asUnsignedByteArray(32, signature.s), new byte[]{signature.v});
    }


    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public String toString() {
        return "WhisperMessage[" +
                "topics=" + Arrays.toString(topics) +
                ", payload=" + (encrypted ? "<encrypted " + payload.length + " bytes>" : new String(payload)) +
                ", to=" + (to == null ? "null" : Hex.toHexString(to).substring(0,16) + "...") +
                ", from=" + (from == null ? "null" : Hex.toHexString(from.getPubKey()).substring(0,16) + "...") +
                ", expire=" + expire +
                ", ttl=" + ttl +
                ", nonce=" + nonce +
                ']';
    }
}
