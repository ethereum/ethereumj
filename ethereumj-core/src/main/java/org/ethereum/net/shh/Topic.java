package org.ethereum.net.shh;

import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * @author by Konstantin Shabalin
 */
public class Topic {
    private byte[] fullTopic;
    private byte[] abrigedTopic = new byte[4];

    public Topic(byte[] data) {
        this.abrigedTopic = data;
    }

    public Topic(String data) {
        fullTopic = sha3(data.getBytes(StandardCharsets.UTF_8));
        this.abrigedTopic = buildAbrigedTopic(fullTopic);
    }

    public byte[] getBytes() {
        return abrigedTopic;
    }

    private byte[] buildAbrigedTopic(byte[] data) {
//        byte[] hash = sha3(data);
        byte[] topic = new byte[4];
        System.arraycopy(data, 0, topic, 0, 4);
        return topic;
    }

    public static Topic[] createTopics(String ... topicsString) {
        Topic[] topics = new Topic[topicsString.length];
        for (int i = 0; i < topicsString.length; i++) {
            topics[i] = new Topic(topicsString[i]);
        }
        return topics;
    }

    public byte[] getAbrigedTopic() {
        return abrigedTopic;
    }

    public byte[] getFullTopic() {
        return fullTopic;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Topic))return false;
        return Arrays.equals(this.abrigedTopic, ((Topic) obj).getBytes());
    }

    @Override
    public String toString() {
        return Hex.toHexString(abrigedTopic);
    }
}
