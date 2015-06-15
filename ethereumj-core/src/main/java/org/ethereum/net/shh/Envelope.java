package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static org.ethereum.net.shh.ShhMessageCodes.MESSAGE;
import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Created by kest on 6/12/15.
 */
public class Envelope extends ShhMessage {

    private long expire;
    private long ttl;

    private Topic[] topics;
    private byte[] data;

    private int nonce = 0;

    public Envelope(byte[] encoded) {
        super(encoded);
    }

    public Envelope(long ttl, Topic[] topics, Message msg) {
        this.expire = System.currentTimeMillis() + ttl;
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

        long sent = this.expire - this.ttl;
        Message m = new Message(data[0], sent, this.ttl, hash());

        if ((m.getFlags() & Message.SIGNATURE_FLAG) == Message.SIGNATURE_FLAG) {
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

        m.decrypt(privKey);

        return m;
    }

    private void parse() {
        if (encoded == null) encode();
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        this.expire = ByteUtil.byteArrayToLong(paramsList.get(0).getRLPData());
        this.ttl = ByteUtil.byteArrayToLong(paramsList.get(1).getRLPData());

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
        byte[] ttl = RLP.encode(this.expire);

        List<byte[]> topics = new Vector<>();
        for (Topic t : this.topics) {
            topics.add(RLP.encodeElement(t.getBytes()));
        }
        byte[][] topicsArray = topics.toArray(new byte[topics.size()][]);
        byte[] encodedTopics = RLP.encodeList(topicsArray);

        byte[] data = RLP.encodeElement(this.data);
        byte[] nonce = RLP.encodeInt(this.nonce);

        this.encoded = RLP.encodeList(expire, ttl, encodedTopics, data, nonce);
    }

    private byte[] encodeWithoutNonce() {
        byte[] expire = RLP.encode(this.expire);
        byte[] ttl = RLP.encode(this.expire);

        List<byte[]> topics = new Vector<>();
        for (Topic t : this.topics) {
            topics.add(RLP.encodeElement(t.getBytes()));
        }
        byte[][] topicsArray = topics.toArray(new byte[topics.size()][]);
        byte[] encodedTopics = RLP.encodeList(topicsArray);

        byte[] data = RLP.encodeElement(this.data);

        return RLP.encodeList(expire, ttl, encodedTopics, data);
    }

    //TODO: complete the nonce implementation
    public void seal(long pow) {
        byte[] d = new byte[64];
        Arrays.fill(d, (byte) 0);
        byte[] rlp = encodeWithoutNonce();

        long then = System.currentTimeMillis() + pow;
        this.nonce = 0;
        for (int bestBit = 0; System.currentTimeMillis() < then; ) {
            for (int i = 0; i < 1024; ++i, ++bestBit) {

            }
        }
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
        return this.toString();
    }
}
