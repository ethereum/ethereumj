package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;

/**
 * Created by kest on 6/13/15.
 */
public class Options {
    private ECKey privateKey;
    private byte[] toPublicKey;
    private Topic[] topics;
    private long ttl;

    public Options(ECKey privateKey, byte[] toPublicKey, Topic[] topics, long ttl) {
        this.privateKey = privateKey;
        this.toPublicKey = toPublicKey;
        this.topics = topics;
        this.ttl = ttl;
    }

    public ECKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(ECKey privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] getToPublicKey() {
        return toPublicKey;
    }

    public void setToPublicKey(byte[] toPublicKey) {
        this.toPublicKey = toPublicKey;
    }

    public Topic[] getTopics() {
        return topics;
    }

    public void setTopics(Topic[] topics) {
        this.topics = topics;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
