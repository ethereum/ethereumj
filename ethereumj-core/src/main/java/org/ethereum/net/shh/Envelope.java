package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

import static org.ethereum.net.shh.ShhMessageCodes.MESSAGE;
import static org.ethereum.crypto.HashUtil.sha3;

/**
 * @author by Konstantin Shabalin
 */
public class Envelope extends ShhMessage {

    private int expire;
    private int ttl;

    private Topic[] topics;
    private byte[] data;

    private int nonce = 0;

    public Envelope(byte[] encoded) {
        super(encoded);
    }

    public Envelope(int ttl, Topic[] topics, Message msg) {
        this.expire = (int)(System.currentTimeMillis()/1000 + ttl);
        this.ttl = ttl;
        this.topics = topics;
        this.data = msg.getBytes();
        this.nonce = 0;
    }

    public Message open(ECKey privKey) {
        if (!parsed) {
            parse();
        }

        byte[] data = this.data;
        int sent = this.expire - this.ttl;
        int flags = data[0] < 0 ? (data[0] & 0xFF) : data[0];

        Message m = new Message(data[0], sent, this.ttl, hash());

        if ((flags & Message.SIGNATURE_FLAG) == Message.SIGNATURE_FLAG) {
            if (data.length < Message.SIGNATURE_LENGTH) {
                throw new Error("Unable to open the envelope. First bit set but len(data) < len(signature)");
            }
            byte[] signature = new byte[Message.SIGNATURE_LENGTH];
            System.arraycopy(data, 1, signature, 0, Message.SIGNATURE_LENGTH);
            m.setSignature(signature);
            byte[] payload = new byte[data.length - Message.SIGNATURE_LENGTH - 1];
            System.arraycopy(data, Message.SIGNATURE_LENGTH + 1, payload, 0, payload.length);
            m.setPayload(payload);
        } else {
            byte[] payload = new byte[data.length - 1];
            System.arraycopy(data, 1, payload, 0, payload.length);
            m.setPayload(payload);
        }

        if (privKey == null) {
            return m;
        }

        if (!m.decrypt(privKey)) {
            return null;
        }

        return m;
    }

    private void parse() {
        if (encoded == null) encode();
        if (isEmpty()) return;

        RLPList paramsList = (RLPList)((RLPList) RLP.decode2(encoded).get(0)).get(0);

        this.expire = ByteUtil.byteArrayToInt(paramsList.get(0).getRLPData());
        this.ttl = ByteUtil.byteArrayToInt(paramsList.get(1).getRLPData());

        List<Topic> topics = new ArrayList<>();
        RLPList topicsList = (RLPList) RLP.decode2(paramsList.get(2).getRLPData()).get(0);
        for (RLPElement e : topicsList) {
            topics.add(new Topic(e.getRLPData()));
        }
        this.topics = new Topic[topics.size()];
        topics.toArray(this.topics);

        this.data = paramsList.get(3).getRLPData();
        this.nonce = ByteUtil.byteArrayToInt(paramsList.get(4).getRLPData());

        this.parsed = true;
    }

    private void encode() {
        byte[] expire = RLP.encode(this.expire);
        byte[] ttl = RLP.encode(this.ttl);

        List<byte[]> topics = new Vector<>();
        for (Topic t : this.topics) {
            topics.add(RLP.encodeElement(t.getBytes()));
        }
        byte[][] topicsArray = topics.toArray(new byte[topics.size()][]);
        byte[] encodedTopics = RLP.encodeList(topicsArray);

        byte[] data = RLP.encodeElement(this.data);
        byte[] nonce = RLP.encodeInt(this.nonce);

        this.encoded = RLP.encodeList(RLP.encodeList(expire, ttl, encodedTopics, data, nonce));
    }

    public void seal(long pow) {
        byte[] d = new byte[64];
        encode();
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

    private byte[] hash() {
        if (encoded == null) encode();
        return sha3(encoded);
    }

    public long getExpire() {
        if(!parsed)  {
            parse();
        }
        return expire;
    }

    public long getTtl() {
        if(!parsed) {
            parse();
        }
        return ttl;
    }

    public Topic[] getTopics() {
        if(!parsed) {
            parse();
        }
        return topics;
    }

    public byte[] getData() {
        if(!parsed) {
            parse();
        }
        return data;
    }

    public boolean isEmpty() {
        if (encoded == null) encode();
        return encoded.length < 2;
    }

    private String topicsToString() {
        StringBuilder topics = new StringBuilder();
        topics.append("[");
        for (Topic t : this.topics) {
            topics.append(Hex.toHexString(t.getBytes()));
            topics.append(", ");
        }
        topics.append("]");
        return topics.toString();
    }

    @Override
    public ShhMessageCodes getCommand() {
        return MESSAGE;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public String toString() {
        if (!parsed) parse();
        if (isEmpty()) {
            return "[" + this.getCommand().name() + " empty envelope]";
        } else {
            return "[" + this.getCommand().name() +
                    " expire=" + this.expire +
                    " ttl=" + this.ttl +
                    " topics=" + topicsToString() +
                    " data=" + Hex.toHexString(this.data) +
                    " nonce=" + Hex.toHexString(new byte[]{(byte) this.nonce}) +
                    "]";
        }
    }
}
