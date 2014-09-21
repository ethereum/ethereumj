package org.ethereum.facade;

import org.ethereum.core.Block;

public interface Blockchain  {

    public int getSize();
    public Block getBlockByNumber(long blockNr);
    public long getGasPrice();
    public Block getLastBlock();
}
