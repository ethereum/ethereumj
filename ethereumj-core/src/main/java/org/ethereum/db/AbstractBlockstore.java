package org.ethereum.db;

import org.ethereum.core.Block;

/**
 * Created by Anton Nashatyrev on 29.10.2015.
 */
public abstract class AbstractBlockstore implements BlockStore {

    @Override
    public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
        Block branchBlock = getBlockByHash(branchBlockHash);
        if (branchBlock.getNumber() < blockNumber) {
            throw new IllegalArgumentException("Requested block number > branch hash number: " + blockNumber + " < " + branchBlock.getNumber());
        }
        while(branchBlock.getNumber() > blockNumber) {
            branchBlock = getBlockByHash(branchBlock.getParentHash());
        }
        return branchBlock.getHash();
    }
}
