package org.ethereum.net.swarm;

/**
 * Created by Admin on 18.06.2015.
 */
public interface Sliced {
    byte[] slice(long from, long to);
}
