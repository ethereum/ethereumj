package org.ethereum.db;

import org.ethereum.core.Block;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigInteger.ZERO;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * @author Roman Mandeleil
 * Created on: 29/01/2015 20:43
 */

public class InMemoryBlockStore implements BlockStore{

    private static final Logger logger = LoggerFactory.getLogger("general");

    Map<ByteArrayWrapper, Block> hashIndex = new HashMap<>();
    Map<Long, Block> numberIndex = new HashMap<>();
    List<Block> blocks = new ArrayList<>();

    SessionFactory sessionFactory;

    BigInteger totalDifficulty = ZERO;

    public InMemoryBlockStore(){
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {

        Block block = numberIndex.get(blockNumber);

        if (block == null)
            return dbGetBlockHashByNumber(blockNumber);
        else
            return block.getHash();
    }

    @Override
    public Block getChainBlockByNumber(long blockNumber) {

        Block block = numberIndex.get(blockNumber);

        if (block == null)
            return dbGetBlockByNumber(blockNumber);
        else
            return block;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {

        Block block = hashIndex.get(wrap(hash));

        if (block == null)
            return dbGetBlockByHash(hash);
        else
            return block;
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        Block block = hashIndex.get(wrap(hash));
        return block != null || dbGetBlockByHash(hash) != null;
    }

    @Override
    public List<byte[]> getListHashesEndWith(byte[] hash, long qty){


        Block startBlock = hashIndex.get(wrap(hash));

        long endIndex = startBlock.getNumber() + qty;
        endIndex = getBestBlock().getNumber() < endIndex ? getBestBlock().getNumber() : endIndex;

        List<byte[]> hashes = new ArrayList<>();

        for (long i = startBlock.getNumber();  i <= endIndex; ++i){
            Block block = getChainBlockByNumber(i);
            hashes.add(block.getHash() );
        }

        return hashes;
    }

    @Override
    public void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain) {
        ByteArrayWrapper wHash = wrap(block.getHash());
        blocks.add(block);
        hashIndex.put(wHash, block);
        numberIndex.put(block.getNumber(), block);
        totalDifficulty = totalDifficulty.add(block.getCumulativeDifficulty());
    }


    @Override
    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    @Override
    public Block getBestBlock() {
        if (blocks.size() == 0) return null;
        return blocks.get(blocks.size() - 1);
    }

    // FIXME: wrap from here in to db class

    public byte[] dbGetBlockHashByNumber(long blockNumber) {

        Session s = sessionFactory.openSession();

        List result = s.createQuery("from BlockVO where number = :number").
                setParameter("number", blockNumber).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return vo.getHash();
    }

    public Block dbGetBlockByNumber(long blockNumber) {

        Session s = sessionFactory.openSession();

        List result = s.createQuery("from BlockVO where number = :number").
                setParameter("number", blockNumber).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        s.close();

        byte[] rlp = vo.getRlp();
        return new Block(rlp);
    }

    public Block dbGetBlockByHash(byte[] hash) {

        Session s = sessionFactory.openSession();

        List result = s.createQuery("from BlockVO where hash = :hash").
                setParameter("hash", hash).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        s.close();
        return new Block(vo.rlp);
    }

    @Override
    public void flush(){

        long t_ = System.nanoTime();

        Session s = sessionFactory.openSession();

        // clear old blocks
        s.beginTransaction();
        s.createQuery("delete from BlockVO").executeUpdate();
        s.getTransaction().commit();

        s.beginTransaction();

        int lastIndex = blocks.size() - 1;
        for (int i = 0;
             i < (blocks.size() > 1000 ? 1000 : blocks.size());
             ++i){

            Block block = blocks.get(lastIndex - i);
            BlockVO blockVO = new BlockVO(block.getNumber(), block.getHash(), block.getEncoded(), block.getCumulativeDifficulty());
            s.save(blockVO);
        }

        s.getTransaction().commit();

        Block block = getBestBlock();

        blocks.clear();
        hashIndex.clear();
        numberIndex.clear();

        saveBlock(block, BigInteger.ZERO, true);

        long t__ = System.nanoTime();
        logger.info("Flush block store in: {} ms", ((float)(t__ - t_) / 1_000_000));

        totalDifficulty =  (BigInteger) s.createQuery("select sum(cumulativeDifficulty) from BlockVO").uniqueResult();

        s.close();
    }

    public void load(){

        logger.info("loading db");

        long t = System.nanoTime();
        Session s = sessionFactory.openSession();

        Long bestNumber = (Long)
                s.createQuery("select max(number) from BlockVO").uniqueResult();

        List result =
                s.createQuery("from BlockVO where number = :number").setParameter("number", bestNumber).list();

        if (result.isEmpty()) return ;
        BlockVO vo = (BlockVO) result.get(0);

        Block bestBlock = new Block(vo.rlp);
        saveBlock(bestBlock, ZERO, true);

        totalDifficulty =  (BigInteger) s.createQuery("select sum(cumulativeDifficulty) from BlockVO").uniqueResult();

        long t_ = System.nanoTime();

        logger.info("Loaded db in: {} ms", ((float)(t_ - t) / 1_000_000));
    }

    @Override
    public long getMaxNumber() {
        Session s = sessionFactory.openSession();

        Long bestNumber = (Long)
                s.createQuery("select max(number) from BlockVO").uniqueResult();

        return bestNumber == null ? 0 : bestNumber;
    }

    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void reBranch(Block forkBlock) {

    }

    @Override
    public BigInteger getTotalDifficultyForHash(byte[] hash) {
        return null;
    }
}

