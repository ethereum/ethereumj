package org.ethereum.trie;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/06/2014 19:44
 */
public interface TrieFacade {

    public void update(byte[] key, byte[] value);
    public byte[] get(byte[] key);
    public void delete(byte[] key);
    
    public byte[] getRootHash();
    
    public String getTrieDump();
}
