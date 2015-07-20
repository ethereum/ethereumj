package org.ethereum.net.swarm;

/**
 *  Any binary data with its key
 *  The key is normally SHA3(data)
 */
public class Chunk {

    protected Key key;
    protected byte[] data;

    public Chunk(Key key, byte[] data) {
        this.key = key;
        this.data = data;
    }

    public Key getKey() {
        return key;
    }

    public byte[] getData() {
        return data;
    }

}
