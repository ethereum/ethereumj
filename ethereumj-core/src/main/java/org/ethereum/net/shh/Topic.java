package org.ethereum.net.shh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Created by kest on 6/12/15.
 */
public class Topic {


    private byte[] topic = new byte[4];

    public Topic(byte[] data) {
        this.topic = data;
    }

    public Topic(String data) {
        this.topic = buildTopic(data.getBytes());
    }

    public byte[] getBytes() {
        return topic;
    }

    private byte[] buildTopic(byte[] data) {
        byte[] hash = sha3(data);
        byte[] topic = new byte[4];
        System.arraycopy(hash, 0, topic, 0, 4);
        return topic;
    }

    public static Topic[] createTopics(String[] topicsString) {
        Topic[] topics = new Topic[topicsString.length];
        for (int i = 0; i < topicsString.length; i++) {
            topics[i] = new Topic(topicsString[i]);
        }
        return topics;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Topic))return false;
        return Arrays.equals(this.topic, ((Topic) obj).getBytes());
    }
}
