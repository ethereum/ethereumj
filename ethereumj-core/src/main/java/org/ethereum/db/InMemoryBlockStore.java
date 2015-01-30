package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.core.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.util.ByteUtil.*;

/**
 * @author: Roman Mandeleil
 * Created on: 29/01/2015 20:43
 */

public class InMemoryBlockStore implements BlockStore{
    
    final static public int MAX_BLOCKS = 1000;

    Map<ByteArrayWrapper, Block> hashIndex = new HashMap<>();
    Map<Long, Block> numberIndex = new HashMap<>();
    List<Block> blocks = new ArrayList<>();
    
    public InMemoryBlockStore(){
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {
        Block block = numberIndex.get(blockNumber);
        if (block == null) return null;
        return block.getHash();
    }

    @Override
    public Block getBlockByNumber(long blockNumber) {
        return numberIndex.get(blockNumber);
    }

    @Override
    public Block getBlockByHash(byte[] hash) {
        return hashIndex.get(wrap(hash));
    }

    @Override
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty) {

        Block startBlock = hashIndex.get(wrap(hash));
        
        long endIndex = startBlock.getNumber() + qty;
        endIndex = getBestBlock().getNumber() < endIndex ? getBestBlock().getNumber() : endIndex;

        List<byte[]> hashes = new ArrayList<>();
        
        for (long i = startBlock.getNumber();  i <= endIndex; ++i){
            Block block = getBlockByNumber(i);
            hashes.add(block.getHash() );
        }
        
        return hashes;
    }

    @Override
    public void deleteBlocksSince(long number) {
        
        // todo: delete blocks sinse
    }

    @Override
    public void saveBlock(Block block, List<TransactionReceipt> receipts) {
        ByteArrayWrapper wHash = wrap(block.getHash());
        blocks.add(block);
        hashIndex.put(wHash, block);
        numberIndex.put(block.getNumber(), block);
        
        if (blocks.size() > MAX_BLOCKS){
            Block rBlock = blocks.remove(0);
            hashIndex.remove(wrap(rBlock.getHash()));
            numberIndex.remove(rBlock.getNumber());
        }
    }

    @Override
    public BigInteger getTotalDifficultySince(long number) {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return BigInteger.ZERO;
    }

    @Override
    public Block getBestBlock() {
        if (blocks.size() == 0) return null;
        return blocks.get(blocks.size() - 1);
    }

    @Override
    public List<Block> getAllBlocks() {
        return blocks;
    }

    @Override
    public void reset() {
        blocks.clear();
        hashIndex.clear();
        numberIndex.clear();
    }

    @Override
    public TransactionReceipt getTransactionReceiptByHash(byte[] hash) {
        return null;
    }
}

