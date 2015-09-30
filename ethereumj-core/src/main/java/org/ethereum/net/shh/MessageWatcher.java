package org.ethereum.net.shh;


import java.util.Arrays;

public abstract class MessageWatcher {
    private byte[] to;
    private byte[] from;
    private Topic[] topics = new Topic[0];

    public MessageWatcher() {
    }

    public MessageWatcher(byte[] to, byte[] from, Topic[] topics) {
        this.to = to;
        this.from = from;
        this.topics = topics;
    }

    public MessageWatcher setTo(byte[] to) {
        this.to = to;
        return this;
    }

    public MessageWatcher setFrom(byte[] from) {
        this.from = from;
        return this;
    }

    public MessageWatcher setFilterTopics(Topic[] topics) {
        this.topics = topics;
        return this;
    }

    Topic[] getTopics() {
        return topics;
    }

    boolean match(byte[] to, byte[] from, Topic[] topics) {
        if (this.to != null) {
            if (!Arrays.equals(this.to, to)) {
                return false;
            }
        }

        if (this.from != null) {
            if (!Arrays.equals(this.from, from)) {
                return false;
            }
        }

        if (this.topics != null) {
            for (Topic watchTopic : this.topics) {
                for (Topic msgTopic : topics) {
                    if (watchTopic.equals(msgTopic)) return true;
                }
            }
            return false;
        }
        return true;
    }

    protected abstract void newMessage(WhisperMessage msg);
}
