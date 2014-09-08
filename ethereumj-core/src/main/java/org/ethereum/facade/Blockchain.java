package org.ethereum.facade;

import org.ethereum.core.Block;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 06/09/2014 08:31
 */

public interface Blockchain  {

    public int getSize();
    public Block getBlockByNumber(long blockNr);
    public long getGasPrice();
    public Block getLastBlock();
}
