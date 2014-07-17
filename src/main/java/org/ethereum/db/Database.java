package org.ethereum.db;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/06/2014 15:38
 */

public interface Database {

    public void put(byte[] key, byte[] value);
    public byte[] get(byte[] key);
    public void delete(byte[] key);

    public void close();
}
