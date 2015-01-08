package org.ethereum.db;

import org.ethereum.core.Block;

/**
 * @author: Roman Mandeleil
 * Created on: 08/01/2015 10:27
 */

public interface BlockStore {

    public byte[] getBlockHashByNumber(long blockNumber);
}
