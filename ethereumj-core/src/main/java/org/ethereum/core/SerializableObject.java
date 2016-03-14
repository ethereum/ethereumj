package org.ethereum.core;

/**
 * Created by Sergio on 03/11/2015.
 */
public interface SerializableObject {

    public byte[] getHash();
    public byte[] getRawHash(); // encoding without any signature
    public byte[] getEncoded();
    public byte[] getEncodedRaw();// encoding without any signature

}
