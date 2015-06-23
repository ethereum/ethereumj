package org.ethereum.net.shh;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Created by kest on 6/12/15.
 */
public class Topic {


    private byte[] topic = new byte[4];

    public Topic(byte[] data) {
        byte[] topic = sha3(data);
        System.arraycopy(topic, 0, this.topic, 0, 4);
    }

    public Topic(String data) {
        this(data.getBytes());
    }

    public byte[] getBytes() {
        return topic;
    }

    public static Topic[] createTopics(String[] topicsString) {
        Topic[] topics = new Topic[topicsString.length];
        for (int i = 0; i < topicsString.length; i++) {
            topics[i] = new Topic(topicsString[i]);
        }
        return topics;
    }
}
