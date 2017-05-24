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
import org.ethereum.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.*;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.net.swarm.Util.rlpDecodeInt;
import static org.ethereum.util.ByteUtil.merge;
import static org.ethereum.util.ByteUtil.xor;

/**
 * Created by Anton Nashatyrev on 25.09.2015.
 */
public class WhisperMessage extends ShhMessage {
    private final static Logger logger = LoggerFactory.getLogger("net.shh");

    public static final int SIGNATURE_FLAG = 1;
    public static final int SIGNATURE_LENGTH = 65;

    private Topic[] topics = new Topic[0];
    private byte[] payload;
    private byte flags;
    private byte[] signature;
    private String to;
    private ECKey from;

    private int expire;
    private int ttl;
    int nonce = 0;

    private boolean encrypted = false;
    private long pow = 0;
    private byte[] messageBytes;

    public WhisperMessage() {
        setTtl(50);
        setWorkToProve(50);
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

    public long getPow() {
        return pow;
    }

    public String getFrom() {
        return from == null ? null : WhisperImpl.toIdentity(from);
    }

    public String getTo() {
        return to;
    }

    /***********   Decode routines   ************/

    private void parse() {
        if (!parsed) {
            RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
            this.expire = ByteUtil.byteArrayToInt(paramsList.get(0).getRLPData());
            this.ttl = ByteUtil.byteArrayToInt(paramsList.get(1).getRLPData());

            List<Topic> topics = new ArrayList<>();
            RLPList topicsList = (RLPList) RLP.decode2(paramsList.get(2).getRLPData()).get(0);
            for (RLPElement e : topicsList) {
                topics.add(new Topic(e.getRLPData()));
            }
            this.topics = new Topic[topics.size()];
            topics.toArray(this.topics);

            messageBytes = paramsList.get(3).getRLPData();
            this.nonce = rlpDecodeInt(paramsList.get(4));
            payload = messageBytes;

            pow = workProved();

            this.parsed = true;
        }
    }

    private boolean processSignature() {
        flags = payload[0];

        if ((flags & WhisperMessage.SIGNATURE_FLAG) != 0) {
            if (payload.length < WhisperMessage.SIGNATURE_LENGTH) {
                throw new RuntimeException("Unable to open the envelope. First bit set but len(data) < len(signature)");
            }
            signature = new byte[WhisperMessage.SIGNATURE_LENGTH];
            System.arraycopy(payload, payload.length - WhisperMessage.SIGNATURE_LENGTH, signature, 0,
                    WhisperMessage.SIGNATURE_LENGTH);
            byte[] msg = new byte[payload.length - WhisperMessage.SIGNATURE_LENGTH - 1];
            System.arraycopy(payload, 1, msg, 0, msg.length);
            payload = msg;
            from = recover();
            return true;
        } else {
            byte[] msg = new byte[payload.length - 1];
            System.arraycopy(payload, 1, msg, 0, msg.length);
            payload = msg;
            return true;
        }
    }

    public boolean decrypt(Collection<ECKey> identities, Collection<Topic> knownTopics) {
        boolean ok = false;
        for (ECKey key : identities) {
            ok = decrypt(key);
            if (ok) break;
        }

        if (!ok) {
        // decrypting as broadcast
            ok = openBroadcastMessage(knownTopics);
        }

        if (ok) {
            return processSignature();
        }

        // the message might be either not-encrypted or encrypted but we have no receivers
        // now way to know so just assuming that the message is broadcast and not encrypted
//        setEncrypted(false);
        return false;
    }

    private boolean decrypt(ECKey privateKey) {
        try {
            payload = ECIESCoder.decryptSimple(privateKey.getPrivKey(), payload);
            to = WhisperImpl.toIdentity(privateKey);
            encrypted = false;
            return true;
        } catch (Exception e) {
            logger.trace("Message can't be opened with key: " + privateKey.getPubKeyPoint());
        } catch (Throwable e) {

        }
        return false;
    }

    private boolean openBroadcastMessage(Collection<Topic> knownTopics) {
        for (Topic kTopic : knownTopics) {
            for (int i = 0; i < topics.length; i++) {
                if (kTopic.equals(topics[i])) {

                    byte[] encryptedKey = Arrays.copyOfRange(payload, i * 2 * 32, i * 2 * 32 + 32);
                    byte[] salt = Arrays.copyOfRange(payload, (i * 2 + 1) * 32, (i * 2 + 2) * 32);
                    byte[] cipherText = Arrays.copyOfRange(payload, (topics.length * 2) * 32, payload.length);
                    byte[] gamma = sha3(xor(kTopic.getFullTopic(), salt));
                    ECKey key = ECKey.fromPrivate(xor(gamma, encryptedKey));
                    try {
                        payload = ECIESCoder.decryptSimple(key.getPrivKey(), cipherText);
                    } catch (Exception e) {
                        logger.warn("Error decrypting message with known topic: " + kTopic);
                        // the abridged topic clash can potentially happen, so just continue with other topics
                        continue;
                    }
                    encrypted = false;
                    return true;
                }
            }
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
            outKey = ECKey.signatureToKey(msgHash, signature);
        } catch (SignatureException e) {
            logger.warn("Exception recovering signature: ", e);
            throw new RuntimeException(e);
        }

        return outKey;
    }

    public byte[] hash() {
        return sha3(payload);
    }

    private int workProved() {
        byte[] d = new byte[64];
        System.arraycopy(sha3(encode(false)), 0, d, 0, 32);
        ByteBuffer.wrap(d).putInt(32, nonce);
        return getFirstBitSet(sha3(d));
    }

    /***********   Encode routines   ************/

    public WhisperMessage setTopics(Topic ... topics) {
        this.topics = topics != null ? topics : new Topic[0];
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
     * If not the message will be encrypted as broadcast with Topics
     * @param to public key
     */
    public WhisperMessage setTo(String to) {
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

    public WhisperMessage setFrom(String from) {
        this.from = WhisperImpl.fromIdentityToPub(from);
        return this;
    }

    public WhisperMessage setTtl(int ttl) {
        this.ttl = ttl;
        expire = (int) (Utils.toUnixTime(System.currentTimeMillis()) + ttl);
        return this;
    }

    public WhisperMessage setWorkToProve(long ms) {
        this.pow = ms;
        return this;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) {
            if (from != null) {
                sign();
            }
            payload = getBytes();
            encrypt();

            byte[] withoutNonce = encode(false);
            nonce = seal(withoutNonce, pow);
            encoded = encode(true);
        }
        return encoded;
    }

    public byte[] encode(boolean withNonce) {
        byte[] expire = RLP.encode(this.expire);
        byte[] ttl = RLP.encode(this.ttl);

        List<byte[]> topics = new Vector<>();
        for (Topic t : this.topics) {
            topics.add(RLP.encodeElement(t.getBytes()));
        }
        byte[][] topicsArray = topics.toArray(new byte[topics.size()][]);
        byte[] encodedTopics = RLP.encodeList(topicsArray);

        byte[] data = RLP.encodeElement(payload);

        byte[] nonce = RLP.encodeInt(this.nonce);

        return withNonce ? RLP.encodeList(expire, ttl, encodedTopics, data, nonce) :
                RLP.encodeList(expire, ttl, encodedTopics, data);
    }

    private int seal(byte[] encoded, long pow) {
        int ret = 0;
        byte[] d = new byte[64];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        System.arraycopy(sha3(encoded), 0, d, 0, 32);

        long then = System.currentTimeMillis() + pow;
        int nonce = 0;
        for (int bestBit = 0; System.currentTimeMillis() < then;) {
            for (int i = 0; i < 1024; ++i, ++nonce) {
                byteBuffer.putInt(32, nonce);
                int fbs = getFirstBitSet(sha3(d));
                if (fbs > bestBit) {
                    ret = nonce;
                    bestBit = fbs;
                }
            }
        }
        return ret;
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

    public byte[] getBytes() {
        if (signature != null) {
            return merge(new byte[]{flags}, payload, signature);
        } else {
            return merge(new byte[]{flags}, payload);
        }
    }

    private void encrypt() {
        try {
            if (to != null) {
                ECKey key = WhisperImpl.fromIdentityToPub(to);
                ECPoint pubKeyPoint = key.getPubKeyPoint();
                payload = ECIESCoder.encryptSimple(pubKeyPoint, payload);
            } else if (topics.length > 0){
                // encrypting as broadcast message
                byte[] topicKeys = new byte[topics.length * 64];
                ECKey key = new ECKey();
                Random rnd = new Random();
                byte[] salt = new byte[32];
                for (int i = 0; i < topics.length; i++) {
                    rnd.nextBytes(salt);
                    byte[] gamma = sha3(xor(topics[i].getFullTopic(), salt));
                    byte[] encodedKey = xor(gamma, key.getPrivKeyBytes());
                    System.arraycopy(encodedKey, 0, topicKeys, i * 64, 32);
                    System.arraycopy(salt, 0, topicKeys, i * 64 + 32, 32);
                }
                ECPoint pubKeyPoint = key.getPubKeyPoint();
                payload = ByteUtil.merge(topicKeys, ECIESCoder.encryptSimple(pubKeyPoint, payload));
            } else {
                logger.debug("No 'to' or topics for outbound message. Will not be encrypted.");
            }
        } catch (Exception e) {
            logger.error("Unexpected error while encrypting: ", e);
        }
        encrypted = true;
    }

    private void sign() {
        flags |= SIGNATURE_FLAG;
        byte[] forSig = hash();

        ECKey.ECDSASignature signature = from.sign(forSig);

        byte v;

        if (signature.v == 27) v = 0;
        else if (signature.v == 28) v = 1;
        else throw new RuntimeException("Invalid signature: " + signature);

        this.signature =
                merge(BigIntegers.asUnsignedByteArray(32, signature.r),
                        BigIntegers.asUnsignedByteArray(32, signature.s), new byte[]{v});
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
                ", to=" + (to == null ? "null" : to.substring(0, 16) + "...") +
                ", from=" + (from == null ? "null" : Hex.toHexString(from.getPubKey()).substring(0,16) + "...") +
                ", expire=" + expire +
                ", ttl=" + ttl +
                ", nonce=" + nonce +
                ']';
    }
}
