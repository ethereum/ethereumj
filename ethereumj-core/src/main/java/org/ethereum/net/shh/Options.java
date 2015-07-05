package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;

/**
 * @author by Konstantin Shabalin
 */
public class Options {

    public static int DEFAULT_TTL = 100;
    public static int DEFAULT_POW = 50;

    private ECKey privateKey;
    private byte[] toPublicKey;
    private Topic[] topics;
    private int ttl;

    public Options(ECKey privateKey, byte[] toPublicKey, Topic[] topics, int ttl) {
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

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
}
